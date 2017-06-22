package cop5556sp17;

import java.io.File;
import java.util.*;

import cop5556sp17.AST.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int argIndex=0;
    java.util.List<java.util.Map.Entry<String,String>> localvars= new java.util.ArrayList<>();

    MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params)
			dec.visit(this, mv);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
        localvars.add(new AbstractMap.SimpleEntry("this",classDesc));
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
//TODO  visit the local variables
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		
		
		cw.visitEnd();//end of class
		
		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
        if(assignStatement.getE().getTypename()==IMAGE) {
            mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageOps.JVMName,"copyImage",PLPRuntimeImageOps.copyImageSig,false);
        }
        CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypename());
		assignStatement.getVar().visit(this, arg);

		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
	    Chain left=binaryChain.getE0();
	    ArrayList<Boolean> args1=new ArrayList<>();
	    args1.add(true);
	    args1.add(false);
        ArrayList<Boolean> args2=new ArrayList<>();
        args2.add(false);
	    left.visit(this,args1);
	    if(left.getTypename()==URL){
            mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageIO.className,"readFromURL",PLPRuntimeImageIO.readFromURLSig,false);
        }
        else if(left.getTypename()==FILE){
            mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageIO.className,"readFromFile",PLPRuntimeImageIO.readFromFileDesc,false);
        }

        if(binaryChain.getArrow().isKind(BARARROW))
            args2.add(true);
        else args2.add(false);
	    ChainElem right=binaryChain.getE1();
	    right.visit(this,args2);

		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
      //TODO  Implement this
        Expression e0=binaryExpression.getE0();
        Expression e1=binaryExpression.getE1();
        e0.visit(this,null);
        e1.visit(this,null);
        Token op=binaryExpression.getOp();
        Label end=new Label();
        Map<Kind,Integer> relOp=new HashMap<Kind,Integer>(){
            {
                put(LT, IF_ICMPLT);
                put(LE, IF_ICMPLE);
                put(GT,IF_ICMPGT);
                put(GE,IF_ICMPGE);
                put(EQUAL,IF_ICMPEQ);
                put(NOTEQUAL,IF_ICMPNE);
            }
        };
        Map<Kind,Integer> wsOp=new HashMap<Kind,Integer>(){
            {
                put(PLUS, IADD);
                put(MINUS, ISUB);
                put(OR,IOR);
                put(TIMES,IMUL);
                put(DIV,IDIV);
                put(AND,IAND);
                put(MOD,IREM);
            }
        };

        if(e0.getTypename()==IMAGE||e1.getTypename()==IMAGE){
            if(op.isKind(PLUS)){
                mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
                        "add", PLPRuntimeImageOps.addSig,false);
            }
            else if(op.isKind(MINUS)){
                mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
                        "sub", PLPRuntimeImageOps.subSig,false);
            }
            else if(op.isKind(DIV)){
                mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
                        "div", PLPRuntimeImageOps.divSig,false);
            }
            else if(op.isKind(TIMES)){
                if(e0.getTypename()==IMAGE){
                    mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
                            "mul", PLPRuntimeImageOps.mulSig,false);
                }
                else{
                    mv.visitInsn(SWAP);
                    mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
                            "mul", PLPRuntimeImageOps.mulSig,false);
                }
            }
            else if(op.isKind(MOD)){
                mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
                        "mod", PLPRuntimeImageOps.modSig,false);
            }
        }
        else {
            if (relOp.containsKey(op.kind)) {
                Label l = new Label();
                mv.visitJumpInsn(relOp.get(op.kind), l);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO, end);

                mv.visitLabel(l);
                mv.visitLdcInsn(true);
            } else if (wsOp.containsKey(op.kind)) {
                mv.visitInsn(wsOp.get(op.kind));
            }
        }
        mv.visitLabel(end);

		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
        Label startBlock = new Label();
        mv.visitLabel(startBlock);

        ArrayList<Dec> decs=block.getDecs();
        ArrayList<Statement> statements=block.getStatements();
        int start=localvars.size();

        for(Dec d:decs)
            d.visit(this,null);
        int end=localvars.size();

        for(Statement s:statements){
            s.visit(this,null);
            if(BinaryChain.class.isInstance(s))
                mv.visitInsn(POP);
        }

        Label endBlock = new Label();
        mv.visitLabel(endBlock);

        for(int i=start;i<end;i++){
            mv.visitLocalVariable(localvars.get(i).getKey(),localvars.get(i).getValue(),null,startBlock,endBlock,i);
        }
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
        mv.visitLdcInsn(booleanLitExpression.getValue().booleanValue());
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
	    if(constantExpression.getFirstToken().isKind(KW_SCREENWIDTH)) {
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig,false);
        }
        else if(constantExpression.getFirstToken().isKind(KW_SCREENHEIGHT)){
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig,false);
        }
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this
        declaration.setSlot(localvars.size());
        Map.Entry<String,String> pair=
                new java.util.AbstractMap.SimpleEntry<>(declaration.getIdent().getText(),
                        declaration.getTypename().getJVMTypeDesc());
        if(declaration.getTypename()==FRAME){
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ASTORE,declaration.getSlot());
        }
        localvars.add(pair);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
	    Token op=filterOpChain.getFirstToken();

        if(((ArrayList<Boolean>)arg).get(1)&&op.isKind(OP_GRAY)){
            mv.visitInsn(DUP);
        }
        else
            mv.visitInsn(ACONST_NULL);

	    if(op.isKind(OP_BLUR)){
            mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeFilterOps.JVMName,"blurOp",PLPRuntimeFilterOps.opSig,false);
        }
        else if(op.isKind(OP_GRAY)){
            mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeFilterOps.JVMName,"grayOp",PLPRuntimeFilterOps.opSig,false);
        }
        else if(op.isKind(OP_CONVOLVE)){
            mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeFilterOps.JVMName,"convolveOp",PLPRuntimeFilterOps.opSig,false);
        }
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
	    frameOpChain.getArg().visit(this,null);
        Token op=frameOpChain.getFirstToken();
        if(op.isKind(KW_HIDE)){
            mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeFrame.JVMClassName,"hideImage",PLPRuntimeFrame.hideImageDesc,false);
        }
        else if(op.isKind(KW_SHOW)){
            mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeFrame.JVMClassName,"showImage",PLPRuntimeFrame.showImageDesc,false);
        }
        else if(op.isKind(KW_MOVE)){
            mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeFrame.JVMClassName,"moveFrame",PLPRuntimeFrame.moveFrameDesc,false);
        }
        else if(op.isKind(KW_XLOC)){
            mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeFrame.JVMClassName,"getXVal",PLPRuntimeFrame.getXValDesc,false);
        }
        else if(op.isKind(KW_YLOC)){
            mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeFrame.JVMClassName,"getYVal",PLPRuntimeFrame.getYValDesc,false);
        }
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
	    //left
        Dec d=identChain.getDec();
        boolean isleft=((ArrayList<Boolean>)arg).get(0);
	    if(isleft){
	        //field
            if(d.getSlot()==-1){
                mv.visitVarInsn(ALOAD,0);
                mv.visitFieldInsn(GETFIELD, className,
                        d.getIdent().getText(),
                        d.getTypename().getJVMTypeDesc());
            }
            //local
            else{
                if(d.getTypename().isType(BOOLEAN)||d.getTypename().isType(TypeName.INTEGER)){
                    mv.visitVarInsn(ILOAD, d.getSlot());
                }
                else{
                    mv.visitVarInsn(ALOAD, d.getSlot());
                }
            }
        }
        //right
        else{
	        if(d.getTypename()==TypeName.INTEGER||d.getTypename()==IMAGE){
                mv.visitInsn(DUP);
                if(d.getSlot()==-1){
                    mv.visitVarInsn(ALOAD,0);
                    mv.visitInsn(SWAP);
                    mv.visitFieldInsn(PUTFIELD, className, d.getIdent().getText(), d.getTypename().getJVMTypeDesc());
                }
                //local
                else{
                    if(d.getTypename().isType(TypeName.INTEGER)){
                        mv.visitVarInsn(ISTORE, d.getSlot());
                    }
                    else{
                        mv.visitVarInsn(ASTORE, d.getSlot());
                    }
                }
            }
            else if(d.getTypename()==FILE){
                if(d.getSlot()==-1) {
                    mv.visitVarInsn(ALOAD,0);
                    mv.visitFieldInsn(GETFIELD, className, d.getIdent().getText(), d.getTypename().getJVMTypeDesc());
                }
                else{
                    mv.visitVarInsn(ALOAD, d.getSlot());
                }
                mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageIO.className,"write",PLPRuntimeImageIO.writeImageDesc,false);
                mv.visitInsn(POP);
                if(d.getSlot()==-1) {
                    mv.visitVarInsn(ALOAD,0);
                    mv.visitFieldInsn(GETFIELD, className, d.getIdent().getText(), d.getTypename().getJVMTypeDesc());
                }
                else{
                    mv.visitVarInsn(ALOAD, d.getSlot());
                }
            }
            else if(d.getTypename()==FRAME){
                mv.visitVarInsn(ALOAD,d.getSlot());
                mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeFrame.JVMClassName,
                        "createOrSetFrame",PLPRuntimeFrame.createOrSetFrameSig,false);
                mv.visitInsn(DUP);
                if(d.getSlot()==-1){
                    mv.visitVarInsn(ALOAD,0);
                    mv.visitInsn(SWAP);
                    mv.visitFieldInsn(PUTFIELD, className, d.getIdent().getText(), d.getTypename().getJVMTypeDesc());
                }
                //local
                else{
                    mv.visitVarInsn(ASTORE, d.getSlot());
                }
            }
        }
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//TODO Implement this
        //field
        if(identExpression.getDec().getSlot()==-1){
            mv.visitVarInsn(ALOAD,0);
            mv.visitFieldInsn(GETFIELD, className,
                    identExpression.getDec().getIdent().getText(),
                    identExpression.getDec().getTypename().getJVMTypeDesc());
        }
        //local
        else{
            if(identExpression.getDec().getTypename().isType(BOOLEAN)||identExpression.getDec().getTypename().isType(TypeName.INTEGER)){
                mv.visitVarInsn(ILOAD, identExpression.getDec().getSlot());
            }
            else{
                mv.visitVarInsn(ALOAD, identExpression.getDec().getSlot());
            }
        }
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//TODO Implement this
        if(identX.getDec().getSlot()==-1){
            mv.visitVarInsn(ALOAD,0);
            mv.visitInsn(SWAP);
            mv.visitFieldInsn(PUTFIELD, className,
                    identX.getDec().getIdent().getText(),
                    identX.getDec().getTypename().getJVMTypeDesc());
        }
        //local
        else{
            if(identX.getDec().getTypename().isType(BOOLEAN)||identX.getDec().getTypename().isType(TypeName.INTEGER)){
                mv.visitVarInsn(ISTORE, identX.getDec().getSlot());
            }
            else{
                mv.visitVarInsn(ASTORE, identX.getDec().getSlot());
            }
        }
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//TODO Implement this
        Label after=new Label();
        ifStatement.getE().visit(this,null);
        mv.visitJumpInsn(IFEQ,after);
        ifStatement.getB().visit(this,null);
        mv.visitLabel(after);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
	    imageOpChain.getArg().visit(this,null);
        Token op=imageOpChain.getFirstToken();
        if(op.isKind(OP_WIDTH)){
            mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeImageIO.BufferedImageClassName,"getWidth",PLPRuntimeImageOps.getWidthSig,false);
        }
        else if(op.isKind(OP_HEIGHT)){
            mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeImageIO.BufferedImageClassName,"getHeight",PLPRuntimeImageOps.getHeightSig,false);
        }
        else if(op.isKind(KW_SCALE)){
            mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageOps.JVMName,"scale",PLPRuntimeImageOps.scaleSig,false);
        }
        return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
        mv.visitLdcInsn(intLitExpression.getFirstToken().intVal());
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		FieldVisitor fv=cw.visitField(ACC_PUBLIC,
                paramDec.getIdent().getText(),paramDec.getTypename().getJVMTypeDesc(),null,null);
        fv.visitEnd();

		if(paramDec.getTypename().isType(TypeName.INTEGER)) {
            mv.visitVarInsn(ALOAD, 0);//this
            mv.visitVarInsn(ALOAD, 1);//args
            mv.visitLdcInsn(argIndex++);//index
            mv.visitInsn(AALOAD);//args[index]
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I",false);
            mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
        }
        else if(paramDec.getTypename().isType(BOOLEAN)){
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(argIndex++);
            mv.visitInsn(AALOAD);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z",false);
            mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
        }
        else if(paramDec.getTypename().isType(URL)){
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(argIndex++);
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig,false);
            mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), PLPRuntimeImageIO.URLDesc);
        }
        else if(paramDec.getTypename().isType(FILE)){
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(NEW, "java/io/File");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(argIndex++);
            mv.visitInsn(AALOAD);
            mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
            mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), PLPRuntimeImageIO.FileDesc);
        }
		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
	    Expression e=sleepStatement.getE();
	    e.visit(this,null);
        mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC,"java/lang/Thread","sleep","(J)V",false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
	    for(Expression e:tuple.getExprList()){
	        e.visit(this,null);
        }
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
        Block b=whileStatement.getB();
        Expression e=whileStatement.getE();
        Label body=new Label();
        Label guard=new Label();
        mv.visitJumpInsn(GOTO, guard);
        mv.visitLabel(body);
        b.visit(this,null);
        mv.visitLabel(guard);
        e.visit(this,null);
        mv.visitJumpInsn(IFNE,body);
		return null;
	}

}
