import java.lang.*;
import java.util.*;
import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

class Kotlin2JavaVisitor <T> extends KotlinBaseVisitor<T>  {

	FileOutputStream fos=null;
    	String output="";
    	int main=0, nested=0, outer=0, rflag=0, mflag=0;
    	String id="", type="", inner="", fun="";
   
    
    	public Kotlin2JavaVisitor(String OutputStream){
		File file=new File(OutputStream);
		try{
			fos=new FileOutputStream(file);
		} catch(IOException e){
			e.printStackTrace();
		}	
    	}

	public static boolean isInteger(String s) {
    		try { 
        		Integer.parseInt(s); 
    		} catch(NumberFormatException e) { 
        		return false; 
    		} catch(NullPointerException e) {
        		return false;
    		}
    		return true;
    	}

	public static boolean isDouble(String s) {
    		try { 
        		Double.parseDouble(s); 
    		} catch(NumberFormatException e) { 
        		return false; 
    		} catch(NullPointerException e) {
        		return false;
    		}
    		return true;
    	}


    	@Override 
    	public T visitProg(KotlinParser.ProgContext ctx) { 
	
		if(ctx.header()!=null) visit(ctx.header());

		int i=0;

		while(ctx.start().object(i)!=null){
			if(ctx.start().object(i).functionDefine()!=null){
				if(ctx.start().object(i).functionDefine().complexID().getText().toString().equals("main")){
					main=1; output+="class Main{\n"; visit(ctx.start()); 
					if(main==1) output+="}\n";
					try{
						fos.write(output.getBytes());
					} catch(IOException e){
						e.printStackTrace();
					}
					return null;
				}
			}
			i++;
		}
		
		visit(ctx.start());
 
		try{
			fos.write(output.getBytes());
		} catch(IOException e){
			e.printStackTrace();
		}

		return null;
    	}

	@Override 
	public T visitHeader(KotlinParser.HeaderContext ctx) { 

		int i=0, point=0;
		while(ctx.packageHeader(i)!=null){
			output+="package "; output+=ctx.packageHeader(i).id().getText(); output+=";\n"; 
			i++;
		}
		output+="\n";
		i=0;
		while(ctx.importHeader(i)!=null){
			output+="import "; point=ctx.importHeader(i).getText().toString().indexOf("\n"); 
			output+=ctx.importHeader(i).getText().toString().substring(6, point); output+=";"; output+="\n";
			i++;
		}
		output+="\n";	
		return null; 
	}


    	@Override 
    	public T visitFunctionDefine(KotlinParser.FunctionDefineContext ctx) {

		if(ctx.complexID().getText().toString().equals("main")){
			if(main==1) output+="\t";
			output+="public static void main(String[] args) {\n\t";
			if(ctx.functionBody().functionContent().contentLine(0).functionDefine()!=null) nested=1;
			mflag=1; visitChildren(ctx); output+="\n\t}\n"; mflag=0; type="";
			return null;	
		}

		char a=ctx.functionBody().getText().toString().charAt(0);

		if(nested==1){
			output+="class Inner {\n\t\t";
			inner=ctx.complexID().getText().toString();
		}
		if(main==1 && nested==0) fun=ctx.complexID().getText().toString();
		if(main==1 || outer==1) output+="\t";
		if(nested==0 && main==1) output+="public static ";
		if(ctx.type()!=null) {
			if(ctx.type().getText().toString().equals("Int?")) output+="Integer ";
			else if(ctx.type().getText().toString().equals("Double?")) output+="Double ";
			else if(ctx.type().getText().toString().equals("Unit")) output+="void ";
			else if(ctx.type().getText().toString().equals("Any")) output+="Object ";			
			else if(ctx.type().getText().toString().equals("String")) output+="String ";
			else output+=ctx.type().getText().toLowerCase()+" ";
		}
		
		else {
			if(a=='=' && ctx.functionParameter().functionParameterList()!=null){
				if(ctx.functionParameter().functionParameterList().type(0).getText().toString().equals("Int?")) output+="Integer ";
				else if(ctx.functionParameter().functionParameterList().type(0).getText().toString().equals("Double?")) output+="Double ";
				else if(ctx.functionParameter().functionParameterList().type(0).getText().toString().equals("Unit")) output+="void ";
				else if(ctx.functionParameter().functionParameterList().type(0).getText().toString().equals("Any")) output+="Object ";
				else if(ctx.functionParameter().functionParameterList().type(0).getText().toString().equals("String")) output+="String ";
				else output+=ctx.functionParameter().functionParameterList().type(0).getText().toLowerCase()+" ";
			}
			else output+="void ";
		}
		
		output+=ctx.complexID().getText();

		if(ctx.functionParameter().functionParameterList()==null) output+="() ";
		
		visitChildren(ctx.functionParameter());
		
		
		output+="{\n\t";
		if(main==1) output+="\t";
		if(nested==1) output+="\t";
		if(a=='=') {
			rflag=1;
			visit(ctx.functionBody());
		}
		if(main==1 && nested==1) output+="\t\t\t";

		if(a!='=')visitChildren(ctx.functionBody());
		
		if(a=='=') {
			output+="\n";
			if(nested==1) output+="\t";
			if(main==1) output+="\t";
			output+="}\n";
		}
		else if(nested==1) output+="\n\t\t\t}\n";
		else if(main==1) output+="\n\t}\n";
		else if(outer==1) output+="\n\t}\n";
		else output+="\n}\n";
		if(nested==1) output+="\t\t}";

		rflag=0;

		return null;
	}



    	@Override 
    	public T visitFunctionParameterList(KotlinParser.FunctionParameterListContext ctx) { 

		int i=0;
		output+="(";

		while(ctx.type(i)!=null){
			if(ctx.type(i).getText().toString().equals("Int?")) output+="Integer ";
			else if(ctx.type(i).getText().toString().equals("Double?")) output+="Double ";
			else if(ctx.type(i).getText().toString().equals("Unit")) output+="void ";
			else if(ctx.type(i).getText().toString().equals("Any")) output+="Object ";
			else if(ctx.type(i).getText().toString().equals("String")) output+="String ";
			else output+=ctx.type(i).getText().toLowerCase()+" ";

			output+=ctx.complexID(i).getText();
			i++;

			if(ctx.type(i)!=null) output+=", ";
			else output+=") ";
		}

		visitChildren(ctx); 

		return null;
    	}

    

    	@Override 
    	public T visitFunctionContent(KotlinParser.FunctionContentContext ctx) { 

		int i=0;
		output+="\t";
		mflag=1;
		while(ctx.contentLine(i)!=null){
			if(main==1) output+="\t";
		
			if(ctx.contentLine(i).RETURN()!=null){
				if(nested==1 && main==1) output+="\t\t";
				else if(outer==1) output+="\t";
				output+=ctx.contentLine(i).RETURN().getText()+" ";
				visit(ctx.contentLine(i).expression());
				output+=";";
			}
			else visit(ctx.contentLine(i));
		
			i++;
			if(ctx.contentLine(i)!=null) output+="\t";
		}
		
		return null;
    	}



    	@Override 
    	public T visitVariableDefine(KotlinParser.VariableDefineContext ctx) {


		if(main==1 && mflag==0) output+="public static ";
		if(ctx.VAL()!=null) output+="final ";

		if(ctx.variableAssn()!=null && ctx.variableAssn().expression()!=null && ctx.variableAssn().expression().functionCall()!=null){
			KotlinParser.ComplexIDContext target=ctx.variableAssn().expression().complexID(0);
			if(target.getText().toString().equals("listOf")){
				output+="List";
				if(ctx.variableAssn().expression().functionCall().functionCallArg(0).doubleMARK()!=null) {
					output+="<String> "; type="String";
				}
				else if(ctx.variableAssn().expression().functionCall().functionCallArg(0).expression()!=null){
					if(ctx.variableAssn().expression().functionCall().functionCallArg(0).expression().num(0)!=null){
						if(isInteger(ctx.variableAssn().expression().functionCall().functionCallArg(0).expression().num(0).getText().toString())){
							output+="<Integer> "; type="Integer";
						}
						else if(isDouble(ctx.variableAssn().expression().functionCall().functionCallArg(0).expression().num(0).getText().toString())){
							output+="<Double> "; type="Double";
						}
						else {
							output+="<Object> "; type="Object";
						}
					}
				}
				else {
					output+="<Object> "; type="Object";
				}
				output+=ctx.complexID().getText();
				output+=" = List.of(";
				visit(ctx.variableAssn().expression().functionCall());
				output+=";";
				return null;
			}
			else if(target.getText().toString().equals("setOf")){
				output+="Set";
				if(ctx.variableAssn().expression().functionCall().functionCallArg(0).doubleMARK()!=null) {
					output+="<String> "; type="String";
				}
				else if(ctx.variableAssn().expression().functionCall().functionCallArg(0).expression()!=null){
					if(ctx.variableAssn().expression().functionCall().functionCallArg(0).expression().num(0)!=null){
						if(isInteger(ctx.variableAssn().expression().functionCall().functionCallArg(0).expression().num(0).getText().toString())){
							output+="<Integer> "; type="Integer";
						}
						else if(isDouble(ctx.variableAssn().expression().functionCall().functionCallArg(0).expression().num(0).getText().toString())){
							output+="<Double> "; type="Double";
						}
						else {
							output+="<Object> "; type="Object";
						}
					}
				}
				else {
					output+="<Object> "; type="Object";
				}
				output+=ctx.complexID().getText();
				output+=" = Set.of(";
				visit(ctx.variableAssn().expression().functionCall());
				output+=";";
				return null;
			}	
		}	
		
	

		if(ctx.type()!=null) {
			if(ctx.type().getText().toString().equals("Int?")) output+="Integer ";
			else if(ctx.type().getText().toString().equals("Double?")) output+="Double ";
			else if(ctx.type().getText().toString().equals("Unit")) output+="void ";
			else if(ctx.type().getText().toString().equals("Any")) output+="Object ";			
			else if(ctx.type().getText().toString().equals("String")) output+="String ";
			else output+=ctx.type().getText().toLowerCase()+" ";
		}
		else {
			if(ctx.variableAssn().expression()!=null){
				if(isInteger(ctx.variableAssn().expression().getText().toString())) output+="int ";
				else if(isDouble(ctx.variableAssn().expression().getText().toString())) output+="double ";
				else if(ctx.variableAssn().expression().num()!=null) output+="int ";
			}
			else if(ctx.variableAssn().doubleMARK()!=null) output+="String ";	
		}

	
		output+=ctx.complexID().getText();

		if(ctx.variableAssn()!=null) {			
			output+=" = ";
			visit(ctx.variableAssn());
		}

		output+=";";
		 
		return null;
    	}


    	@Override 
    	public T visitForExpr(KotlinParser.ForExprContext ctx) { 
		output+="for (";
		visit(ctx.forCondition());
		output+=") ";
		visit(ctx.forBody());

		return null;
    	}


    	@Override 
    	public T visitForCondition(KotlinParser.ForConditionContext ctx) {

		int point=ctx.conditionFrag(0).getText().toString().indexOf("downTo");
		if(ctx.conditionFrag(0).expression(1)==null){
			point=ctx.conditionFrag(0).expression(0).getText().toString().indexOf(".indices");
			if(point>0) {
				output+="int "; output+=ctx.conditionFrag(0).complexID(0).getText(); output+=" = 0 ; ";
				output+=ctx.conditionFrag(0).complexID(0).getText(); output += " < ";
				output+=ctx.conditionFrag(0).expression(0).complexID(0).getText().toString().substring(0, point);
				output+=".size(); "; output+=ctx.conditionFrag(0).complexID(0).getText(); output+="++"; point=0;
				return null;
			}
			output+=type; output+=" "; output+=ctx.conditionFrag(0).complexID(0).getText(); output+=" : ";
			output+=ctx.conditionFrag(0).expression(0).getText();
		}
	
		else{
			
			if(isInteger(ctx.conditionFrag(0).expression(0).getText().toString())) output+="int ";
			else if(isInteger(ctx.conditionFrag(0).expression(1).getText().toString())) output+="int ";
			else if(isDouble(ctx.conditionFrag(0).expression(0).getText().toString())) output+="double ";
			
			output+=ctx.conditionFrag(0).complexID(0).getText(); output+=" = ";
			output+=ctx.conditionFrag(0).expression(0).getText();output+="; ";
			output+=ctx.conditionFrag(0).complexID(0).getText();
			if(point>0) output+=" >= "; 
			else if(ctx.conditionFrag(0).expression(0)!=null){
				if(Integer.parseInt(ctx.conditionFrag(0).expression(0).getText().toString())>Integer.parseInt(ctx.conditionFrag(0).expression(1).getText().toString())) 
					output+=" >= ";
				else output+=" <= ";
			}
			output+=ctx.conditionFrag(0).expression(1).getText(); output+="; ";
			if(ctx.conditionFrag(0).expression(2)==null){
				output+=ctx.conditionFrag(0).complexID(0).getText(); 
				if(point>0) output+="--";
				else if(ctx.conditionFrag(0).expression(0)!=null) {
					if(Integer.parseInt(ctx.conditionFrag(0).expression(0).getText().toString())>Integer.parseInt(ctx.conditionFrag(0).expression(1).getText().toString())) 
						output+="--";
					else output+="++";
				}
			}
			else {
				output+=ctx.conditionFrag(0).complexID(0).getText(); output+="="; output+=ctx.conditionFrag(0).complexID(0).getText();
				if(point>0) output+="-";
				else if(ctx.conditionFrag(0).expression(0)!=null){
					if(Integer.parseInt(ctx.conditionFrag(0).expression(0).getText().toString())>Integer.parseInt(ctx.conditionFrag(0).expression(1).getText().toString())) 
						output+="-";
					else output+="+";
				}
				else output+="+";
				output+=ctx.conditionFrag(0).expression(2).getText();
			}
		}
		point=0;
		
	 
		return null;
    	}
  

    	@Override 
    	public T visitForBody(KotlinParser.ForBodyContext ctx) { 
		
		int i=0;
		output+="{\n\t";
		
		while(ctx.contentLine(i)!=null){
			output+="\t\t";
		
			if(ctx.contentLine(i).RETURN()!=null){
				output+="return "; visit(ctx.contentLine(i).expression()); output+=";";
			}
			else visit(ctx.contentLine(i));
		
			i++;
			if(ctx.contentLine(i)!=null) output+="\t";
		}
		output+="\t\t}";

		return null; 
    	}

	@Override 
	public T visitWhileExpr(KotlinParser.WhileExprContext ctx) { 
		output+="while (";
		visit(ctx.whileCondition());
		output+=") ";
		visit(ctx.whileBody());

		return null; 
	}

	@Override 
	public T visitWhileCondition(KotlinParser.WhileConditionContext ctx) { 
		output+=ctx.conditionFrag(0).getText();
		int point=0;
		if(ctx.conditionFrag(0).complexID(1)!=null){
			point=ctx.conditionFrag(0).complexID(1).getText().toString().indexOf(".");
			if(point>0) output+="()";
		}
		return null; 
	}

	@Override 
	public T visitWhileBody(KotlinParser.WhileBodyContext ctx) { 
		int i=0;
		output+="{\n\t";
		
		while(ctx.contentLine(i)!=null){
			output+="\t\t";
		
			if(ctx.contentLine(i).RETURN()!=null){
				output+="return "; visit(ctx.contentLine(i).expression()); output+=";";
			}
			else visit(ctx.contentLine(i));
		
			i++;
			if(ctx.contentLine(i)!=null) output+="\t";
		}
		output+="\t\t}";

		return null;  
	}

    	@Override 
   	public T visitIfExpr(KotlinParser.IfExprContext ctx) {
 
		if(nested==1 && main==1) output+="\t\t";
		else if(outer==1) output+="\t";
		output+="if (";
		visit(ctx.ifCondition());
		output+=") {\n";
		visit(ctx.ifBody());
		output+="\n";
		if(nested==1 && main==1) output+="\t\t";
		output+="\t\t}";
		if(ctx.ELSE()!=null) {
			output+=" else { \n"; visit(ctx.elseBody()); output+="\n";
			if(nested==1 && main==1) output+="\t\t";
			output+="\t\t}";
		}
		output+="\n";
		return null;
    	}

    	@Override 
    	public T visitIfBody(KotlinParser.IfBodyContext ctx) {

		if(nested==1 && main==1) output+="\t\t";
 		if(ctx.contentLine(0).RETURN()!=null) output+="\t\t\treturn ";
		if(ctx.contentLine(0).expression()==null){
			if(main==1) output+="\t";
			if(nested==1) output+="\t";
			if(rflag==1) output+="\t\treturn ";
			output+=ctx.contentLine(0).objectDefine().getText(); output+=";";
			return null;
		}
		String s=ctx.contentLine(0).expression().complexID(0).getText().toString();
		int point=s.indexOf(".")+1;
	
		if(s.substring(point).equals("length")) {
			output+="((String) "; output+=s.substring(0, point-1); output+=").length();";
		}
		else {
			output+=ctx.contentLine(0).expression().complexID(0).getText();
			output+=";";
		}
	
		return null;
	}

	@Override 
    	public T visitElseBody(KotlinParser.ElseBodyContext ctx) {

		if(nested==1 && main==1) output+="\t\t";
 		if(ctx.contentLine(0).RETURN()!=null) output+="\t\t\treturn ";
		if(ctx.contentLine(0).expression()==null){
			if(main==1) output+="\t";
			if(nested==1) output+="\t";
			if(rflag==1) output+="\t\treturn ";
			output+=ctx.contentLine(0).objectDefine().getText(); output+=";";
			return null;
		}
		String s=ctx.contentLine(0).expression().complexID(0).getText().toString();
		int point=s.indexOf(".")+1;
	
		if(s.substring(point).equals("length")) {
			output+="((String) "; output+=s.substring(0, point-1); output+=").length();";
		}
		else {
			output+=ctx.contentLine(0).expression().complexID(0).getText();
			output+=";";
		}
	
		return null;
	}

    	@Override 
    	public T visitIfCondition(KotlinParser.IfConditionContext ctx) {

		int i=0, point=0, clen=0;
		while(ctx.conditionFrag(i)!=null){
			if(ctx.conditionFrag(i).type(0)!=null){
				output+=ctx.conditionFrag(i).complexID(0).getText();
				output+=" instanceof "; output+=ctx.conditionFrag(i).type(0).getText();
			}
			else if(ctx.conditionFrag(i).complexID(1)==null){
				point=ctx.conditionFrag(i).complexID(0).getText().toString().indexOf(".");
				clen=ctx.conditionFrag(i).complexID(0).getText().toString().length();
				output+=ctx.conditionFrag(i).complexID(0).getText();
				if(point>0) output+="() ";
				output+=ctx.conditionFrag(i).getText().toString().substring(clen);
			}
			else output+=ctx.conditionFrag(i).getText();
			i++;
			if(ctx.conditionFrag(i)!=null) output+=" && ";
		}
	 
		return null;
	}

    	@Override 
    	public T visitWhenExpr(KotlinParser.WhenExprContext ctx) {

		int i=0, point=0;
		String s="";

		output+="switch("; output+=ctx.whenCondition().getText(); output+=") {\n";
	
		while(ctx.whenBody().whenContent(i)!=null){
			output+="\t\t\t"; 
			if(ctx.whenBody().whenContent(i).former().getText().toString().equals("else")) output+="default";
			else {
				output+="case "; output+=ctx.whenBody().whenContent(i).former().getText();
			}
			output+=":\t ";
			s=ctx.whenBody().whenContent(i).latter().contentLine().getText().toString();
			if(s.substring(0, 6).equals("return")){
				output+="return ";
				if(ctx.whenBody().whenContent(i).latter().contentLine().doubleMARK()!=null)
					output+=ctx.whenBody().whenContent(i).latter().contentLine().doubleMARK().getText();
				else if(ctx.whenBody().whenContent(i).latter().contentLine().expression()!=null)
					output+=ctx.whenBody().whenContent(i).latter().contentLine().expression().getText();
				output+=";";
			}
			else{
				visit(ctx.whenBody().whenContent(i).latter().contentLine().objectDefine());
			}
			i++;
			if(ctx.whenBody().whenContent(i)!=null) output+="\n";
		}
		output+="\n\t\t}";
	
		return null; 
	}

    	@Override 
    	public T visitObjectDefine(KotlinParser.ObjectDefineContext ctx) { 

		if(ctx.complexID().getText().toString().equals("println")){
			output+="System.out.println(";
			if(nested==1) {
				if(ctx.functionCall().functionCallArg(0).expression().complexID(0).getText().toString().equals(inner))
					output+="new Inner().";
				else if(!ctx.functionCall().functionCallArg(0).expression().complexID(0).getText().toString().equals(fun)){
					output+="new "; output+= ctx.functionCall().functionCallArg(0).expression().getText(); output+=");";
					return null;
				}
			}
			visit(ctx.functionCall());
			output+=";";
		}
		else if(ctx.complexID().getText().toString().equals("print")){
			output+="System.out.print(";
			if(nested==1) {
				if(ctx.functionCall().functionCallArg(0).expression().complexID(0).getText().toString().equals(inner))
					output+="new Inner().";
				else if(!ctx.functionCall().functionCallArg(0).expression().complexID(0).getText().toString().equals(fun)){
					output+="new "; output+= ctx.functionCall().functionCallArg(0).expression().getText(); output+=");";
					return null;
				}
			}
			visit(ctx.functionCall());
			output+=";";
		}
		else{
			output+=ctx.complexID().getText();

			if(ctx.lambdaExpr()!=null) {
				output+=".stream()."; visit(ctx.lambdaExpr());
			}
	
			if(ctx.variableAssn()!=null){
				output+=" "; output+=ctx.variableAssn().operator().getText()+" ";
			}
		
			if(ctx.variableAssn()!=null) visit(ctx.variableAssn()); 
			else if(ctx.functionCall()!=null) {
				output+="("; visit(ctx.functionCall());
			}
			output+=";\n";
		}
	
		return null;
	}

    	@Override 
    	public T visitLambdaExpr(KotlinParser.LambdaExprContext ctx) { 
		
		int i=0;
		while(ctx.lambdaElement(i)!=null){
			if(ctx.lambdaElement(i).simpleID().getText().toString().equals("sortedBy")) output+="sorted";
			else output+=ctx.lambdaElement(i).simpleID().getText();
			output+="(";
			if(!ctx.lambdaElement(i).complexID().getText().toString().equals("it")){
				output+="it -> ";
				if(ctx.lambdaElement(i).complexID().getText().toString().equals("println")){
					output+="System.out.println("; visit(ctx.lambdaElement(i).functionCall());
				}
				else {
					output+=ctx.lambdaElement(i).complexID().getText();
					output+=ctx.lambdaElement(i).functionCall().getText();
				}
			}
			output+=")";
			
			i++;
			if(ctx.lambdaElement(i)!=null) output+="\n\t\t\t\t.";
		}
	
		
		return null; 
    	}

    
	@Override 
    	public T visitExpression(KotlinParser.ExpressionContext ctx) {
	
		int i=0; 
		int point=-1;
		if(rflag==1) output+="return ";
		if(ctx.getText().toString().equals("null")) {
			output+="null";
			return null;
		}
		if(ctx.num(0)!=null && ctx.operator(0)!=null && ctx.complexID(0)!=null) {
			output+=ctx.getText();
			return null;
		}
		while(ctx.complexID(i)!=null){
			output+=ctx.complexID(i).getText();
			if(ctx.functionCall()!=null) {
				point=ctx.functionCall().getText().toString().indexOf("[");
				if(point==0) output+=".get";
				point=-1;
				output+="("; visit(ctx.functionCall());
			}
	
			i++;
			if(ctx.complexID(i)!=null) output+=" "+ctx.operator(i-1).getText()+" ";
		}
		i=0;
		while(ctx.num(i)!=null){
			output+=ctx.num(i).getText();
			i++;
			if(ctx.num(i)!=null) output+=" "+ctx.operator(i-1).getText()+" ";
		}
		if(rflag==1) output+=";";
		return null;
    	}

    	@Override 
    	public T visitDoubleMARK(KotlinParser.DoubleMARKContext ctx) { 
		if(ctx.markContent().frag(0)==null) output+="\""; 
		else if(ctx.markContent().frag(0).mFrag()!=null) output+="\"";
		visit(ctx.markContent());
	
		return null; 
    	} 

    	@Override 
    	public T visitMarkContent(KotlinParser.MarkContentContext ctx) { 
		int i=0;
		int point=0;
		while(ctx.frag(i)!=null){
			if(ctx.frag(i).mFrag()!=null) {
				if(ctx.frag(i-1)!=null && ctx.frag(i-1).dFrag()!=null) {
					output+=" + \"";
					if(!ctx.frag(i).mFrag().getText().toString().equals(",") && !ctx.frag(i).mFrag().getText().toString().equals("!") 
						&& !ctx.frag(i).mFrag().getText().toString().equals(";") && !ctx.frag(i).mFrag().getText().toString().equals(".") 
						&& !ctx.frag(i).mFrag().getText().toString().equals("?") && !ctx.frag(i).mFrag().getText().toString().equals(":")) output+=" ";
				}
				output+=ctx.frag(i).mFrag().getText();
			}
			else {
				if(ctx.frag(i-1)!=null && ctx.frag(i-1).mFrag()!=null) output+="\" + ";

				if(ctx.frag(i).dFrag().expression()!=null) {
					if(ctx.frag(i).dFrag().expression().operator()!=null) output+="(";
					visit(ctx.frag(i).dFrag());
					if(ctx.frag(i).dFrag().expression().operator()!=null) output+=")";
				}
				else output+=ctx.frag(i).dFrag().complexID().getText();
			}

			i++;

			if(ctx.frag(i)!=null){
				if(ctx.frag(i-1).mFrag()!=null){
					if(ctx.frag(i).mFrag()!=null){
						 if(!ctx.frag(i).mFrag().getText().toString().equals(",") && !ctx.frag(i).mFrag().getText().toString().equals("!") 
							&& !ctx.frag(i).mFrag().getText().toString().equals(";") && !ctx.frag(i).mFrag().getText().toString().equals(".")
							&& !ctx.frag(i).mFrag().getText().toString().equals("?") && !ctx.frag(i).mFrag().getText().toString().equals(":")) output+=" ";
					}
					else output+=" ";
				}
			}
			else if (ctx.frag(i-1).mFrag()!=null) {
				if(i==1) output+="\"";
				else output+=" \"";
			}
		}
		if(i==0) output+=" \"";
		return null; 
    	}

    	@Override 
    	public T visitFunctionCall(KotlinParser.FunctionCallContext ctx) {
		
		int i=0;
	
		while(ctx.functionCallArg(i)!=null){
			visit(ctx.functionCallArg(i));
			i++;

			if(ctx.functionCallArg(i)!=null) output+=", ";
		}
		output+=")";
	
		return null;
    	}

	@Override 
	public T visitClassDefine(KotlinParser.ClassDefineContext ctx) {
		if(main==1) output+="\n}\n\n";
		main=0; nested=0; outer=1;
		output+="class "; output+=ctx.complexID().getText(); 
		output+= " {\n"; visit(ctx.classBody()); output+="}\n";
		outer=0;
		return null;
	}
	
    
    	@Override 
    	public T visitNewLine(KotlinParser.NewLineContext ctx) { 
		output+="\n";
		return null; 
    	}
    
}

public class Kotlin2Java {
	public static void main(String[] args) throws IOException {
      
	
		FileInputStream filePath = new FileInputStream(args[0]);

		int point=args[0].indexOf(".");
		String outputStream=args[0].substring(0, point)+".java";
       	
		if(args.length == 2) outputStream=args[1];

	
      		KotlinLexer lexer = new KotlinLexer(new ANTLRInputStream(filePath));
      		CommonTokenStream tokens = new CommonTokenStream(lexer);
      		KotlinParser parser = new KotlinParser(tokens);
      		ParseTree tree = parser.prog();
		Kotlin2JavaVisitor visitor = new Kotlin2JavaVisitor(outputStream);

		visitor.visit(tree);	
    	}
}

