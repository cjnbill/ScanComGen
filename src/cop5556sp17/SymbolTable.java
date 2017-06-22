package cop5556sp17;



import cop5556sp17.AST.Dec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;


public class SymbolTable {
	
	
	//TODO  add fields

	HashMap<String,HashMap<Integer,Dec>> map;
	Stack<Integer> scope_stack;
    int  current_scope, next_scope;
    /**
	 * to be called when block entered
	 */
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
        current_scope = next_scope++;
        scope_stack.push(current_scope);
    }
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
        scope_stack.pop();
        if(scope_stack.isEmpty())
            current_scope=-1;
        else current_scope = scope_stack.peek();
	}
	
	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
        HashMap<Integer,Dec> value = map.get(ident);
        if (value != null) {
            Dec v=value.get(current_scope);
            if(v!=null)return false;
            else value.put(current_scope,dec);
        } else {
            // No such key
            HashMap<Integer,Dec> tmp=new HashMap<Integer,Dec>();
            tmp.put(current_scope,dec);
            map.put(ident,tmp);
        }
		return true;
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
        HashMap<Integer,Dec> ent=map.get(ident);
        if(ent==null)return null;
        int mindiff=Integer.MAX_VALUE;
        int k=0;
        int top=scope_stack.peek();
        for (Integer key : ent.keySet()) {
            int diff=top-key;
            if(diff>=0&&diff<mindiff){
                mindiff=diff;
                k=key;
            }
        }
        if(mindiff==Integer.MAX_VALUE)return null;
		return ent.get(k);
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
        scope_stack=new Stack<Integer>();
        map=new HashMap<>();
        current_scope=-1;
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return "";
	}
	
	


}
