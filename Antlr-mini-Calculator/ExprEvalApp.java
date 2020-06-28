import java.lang.*;
import java.util.*;
import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

class EvalListener extends ExprBaseListener {

    // hash-map for variables' double value for assignment
    Map <String, Double> vars = new HashMap <String, Double> ();

    // stack for expression tree evaluation
    Stack <Object> evalStack = new Stack <Object> ();

    // stack for operators (+-*/()) 
    Stack <String> opStack = new Stack <String> ();

    boolean isAssn = false;
    boolean isUnary = false;
    String recentUnary = "";

    @Override
    public void exitProg(ExprParser.ProgContext ctx) {
        //System.out.println("exitProg: ");
    }


    @Override
    public void exitExpr(ExprParser.ExprContext ctx) {
        if (ctx.getParent().getParent()==null) evalExprTree();       
    }


    @Override
    public void enterAssn(ExprParser.AssnContext ctx) {
        vars.put(ctx.var().getText(), Double.parseDouble(ctx.num().getText()));
        isAssn = true;
    }

    @Override
    public void exitAssn(ExprParser.AssnContext ctx) {
        isAssn = false;
    }

    @Override
    public void enterNum(ExprParser.NumContext ctx) {
        isUnary = true;
        recentUnary = "";
    }

    @Override
    public void exitNum(ExprParser.NumContext ctx) {
        isUnary = false;
        recentUnary = "";
    }


    // adopting Shunting-Yard algorithm
    @Override
    public void visitTerminal(TerminalNode node) {

        String token = node.getText();
	String topOp;

        if (isUnary == true && (token.equals("+") || token.equals("-"))) {
            recentUnary = token;
            return;
        }

        if (token.equals("(") || token.equals(")")){
	    if (opStack.empty()) opStack.push(token);
	    else if (token.equals("(")) opStack.push(token);
	    else if (opStack.lastElement().equals("(")) opStack.push(token);
            else if (token.equals(")")) {
		while (opStack.lastElement().equals("(")==false){
		    topOp=opStack.pop();
                    evalStack.push(topOp);
	        }	
            	opStack.pop();
            } 
	}   
	else if(token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/")){
	    if (opStack.empty()) opStack.push(token);
	    else if(opStack.lastElement().equals("(")) opStack.push(token);
	    else if((token.equals("*") || token.equals("/")) && (opStack.lastElement().equals("+") || opStack.lastElement().equals("-")))
		opStack.push(token);
            else {
		topOp=opStack.pop();
            	evalStack.push(topOp);
            	while(opStack.empty()==false){
		    topOp=opStack.pop();
		    evalStack.push(topOp);
		}
		opStack.push(token);
            }
	}   		

        else if (isAssn == false) {
            if (isUnary==true) { 
                double oprd = Double.parseDouble(token);
                if (recentUnary.equals("-")) {
                    oprd -= 2 * oprd;
                    evalStack.push(oprd);
                } 
                else 
                    evalStack.push(oprd);
            } 
            else if (token.matches("[a-zA-Z]+")) {
                if (vars.containsKey(token)) evalStack.push(vars.get(token));
                else return;
            }
        }
    }


    public void evalExprTree() {

	while (opStack.isEmpty()==false) {
	    String topOp=opStack.pop();
            evalStack.push(topOp);
        }

	int stackSize=evalStack.size();

	while(stackSize>1){
	    int cur=0;
            while(cur<stackSize){

		String s = evalStack.get(cur).toString();
                double oprd1, oprd2;

            	if (s.equals("+")) {
		    evalStack.remove(cur);
                    oprd2 = Double.parseDouble(evalStack.remove(cur - 1).toString());
                    oprd1 = Double.parseDouble(evalStack.remove(cur - 2).toString());

                    evalStack.add(cur - 2, oprd1 + oprd2);
                    break;
            	} 
            	else if (s.equals("-")) {
		    evalStack.remove(cur);
                    oprd2 = Double.parseDouble(evalStack.remove(cur - 1).toString());
                    oprd1 = Double.parseDouble(evalStack.remove(cur - 2).toString());

                    evalStack.add(cur - 2, oprd1 - oprd2);
                    break;
            	} 
            	else if (s.equals("*")) {
		    evalStack.remove(cur);
                    oprd2 = Double.parseDouble(evalStack.remove(cur - 1).toString());
                    oprd1 = Double.parseDouble(evalStack.remove(cur - 2).toString());

               	    evalStack.add(cur - 2, oprd1 * oprd2);
                    break;
            	} 
            	else if (s.equals("/")) {
		    evalStack.remove(cur);
                    oprd2 = Double.parseDouble(evalStack.remove(cur - 1).toString());
                    oprd1 = Double.parseDouble(evalStack.remove(cur - 2).toString());

                    evalStack.add(cur - 2, (Double) oprd1 / oprd2);
                    break;
            	}
	    	cur++;
	    }
	    stackSize=evalStack.size();
	}

        System.out.println(Double.parseDouble(evalStack.remove(0).toString()));
    }
}

public class ExprEvalApp {
    public static void main(String[] args) throws IOException {
        //System.out.println("** Expression Eval w/ antlr-listener **");
      
	FileInputStream filePath = null;
		
       	if(args.length == 1){
            filePath  = new FileInputStream(args[0]);
       	}

	else {
	    Console c = System.console();
            String input = c.readLine("File Path: ");
            filePath = new FileInputStream(input);
       	}


      	// Get lexer
      	ExprLexer lexer = new ExprLexer(new ANTLRInputStream(filePath));
      	// Get a list of matched tokens
      	CommonTokenStream tokens = new CommonTokenStream(lexer);
      	// Pass tokens to parser
      	ExprParser parser = new ExprParser(tokens);
      	// Walk parse-tree and attach our listener
      	ParseTreeWalker walker = new ParseTreeWalker();
      	EvalListener listener = new EvalListener();

	// walk from the root of parse tree
      	walker.walk(listener, parser.prog());	
    }
} 
