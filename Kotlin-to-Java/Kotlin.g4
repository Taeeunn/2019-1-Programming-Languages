/* Kotlin.g4 */
grammar Kotlin;




// parser rules
prog: newLine* header? start EOF ;
start: newLine* object* ;
header: (packageHeader | importHeader)+ ; 
object: (functionDefine | variableDefine | objectDefine | ifExpr | forExpr | whileExpr | whenExpr | classDefine | interfaceDefine) newLine? ;

packageHeader: PACKAGE id newLine? ; 
importHeader: IMPORT id '.'? '*'? newLine? ;

functionDefine: (ABSTRACT | OVERRIDE)? FUN complexID functionParameter ':'? type? functionBody ;
functionParameter: '(' functionParameterList? ')' ; 
functionParameterList: complexID ':' type (',' complexID ':' type)* ;
functionBody: '{'? newLine? (functionContent '}'? | '=' newLine? (expression | whenExpr | ifExpr)) ;
functionContent: contentLine* ;

expression: (complexID | type | NullLiteral | num) (functionCall '.'? expression? | (operator (element | complexID | num))+)? ('?'':' str)? ;
functionCall: ('(' | '[') (functionCallArg | num)* (',' (functionCallArg | num))* (')' | ']') ;
functionCallArg: (doubleMARK | expression) ;
element: '(' complexID '-' complexID ')' ;
str: (ID | IS | '"' | '.' | ',')* ;

variableDefine: (ABSTRACT | OVERRIDE)? (VAL|VAR) complexID ':'? type? (GET functionCall)? variableAssn? ;
variableAssn:  operator (doubleMARK | expression)? ; 

objectDefine: (complexID | type) (functionCall | variableAssn | newLine? lambdaExpr)?;

ifExpr: IF '(' ifCondition ')' ifBody newLine? (ELSE elseBody)? ;
ifBody: ('{' newLine? contentLine* '}' | newLine? contentLine?) ;
elseBody: ('{' newLine? contentLine* '}' | newLine? contentLine?) ;

forExpr: FOR '(' forCondition ')' (forBody | newLine? contentLine?) ;
forBody: '{' newLine? contentLine* '}' ;

whileExpr: WHILE '(' whileCondition ')' (whileBody | newLine? contentLine?) ;
whileBody: '{' newLine? contentLine* '}' ;

whenExpr: WHEN ('(' whenCondition ')')? whenBody ;
whenBody: '{' newLine? whenContent* '}' ;
whenContent: former '->' latter newLine? ;
former: num | doubleMARK IN? simpleID? | '!'? IS type | ELSE ;
latter: contentLine;

lambdaExpr: lambdaElement+ ;
lambdaElement: '.' simpleID '{' complexID functionCall? '}' newLine? ; 

classDefine: ABSTRACT? CLASS complexID classParameter classInheritance? classBody ;
classParameter: '(' newLine? classParameterList? newLine? ')' ; 
classParameterList: parameterElement (',' newLine? parameterElement)* ;
parameterElement: (VAR|VAL) simpleID ':' (type | 'List''<' type '>') ;
classInheritance: ':' inheritanceElement (',' inheritanceElement)* ;
inheritanceElement: complexID functionCall? ;
classContent: ((variableDefine | functionDefine) newLine?) ;
classBody: '{' newLine? classContent* '}' ;


interfaceDefine: INTERFACE complexID interfaceBody ;
interfaceBody: '{' newLine? contentLine* '}' ;


ifCondition: conditionFrag ('&''&' conditionFrag)* ;
forCondition: conditionFrag ('&''&' conditionFrag)* ;
whileCondition: conditionFrag ('&''&' conditionFrag)* ;
whenCondition: conditionFrag ('&''&' conditionFrag)* ;
conditionFrag: ((complexID|num) '!'? (IN expression? '.'?'.'? expression? 'downTo'? expression? 'step'? expression? | IS type)? (('>'|'<') (complexID | num) | '!''=' NullLiteral | '=''=' NullLiteral)? )* ;

contentLine: (RETURN (expression | doubleMARK)? | objectDefine | variableDefine | ifExpr | forExpr | whileExpr | whenExpr | functionDefine | doubleMARK) newLine? ;
id: simpleID ('.' simpleID)* ;
complexID: ID ('.' ID)* ('.''*')? INT? ;
simpleID : ID ;
num: ('+' | '-')? (INT | REAL) ;
type: TYPE ;
newLine: NEWLINE+ ;
operator: '=' | '==' | '+=' | '-=' | '*=' | '/=' | '++' | '--' | '+' | '-' | '*' | '/' ;


doubleMARK: '"' markContent '"' ;
markContent: (frag)* ;
frag: mFrag | dFrag ;
mFrag: ID functionCall? | num | IS | IN | type | '=' | ';' | ':' | '\'' | ',' | '*' | '!' | '?' | '.' ; 
dFrag: '$' (complexID | '{' expression '}') ;




// lexer rules
PACKAGE: 'package' ;
IMPORT: 'import' ;
FUN: 'fun' ;
RETURN: 'return' ;
VAL: 'val' ;
VAR: 'var' ;
IF: 'if' ;
ELSE: 'else' ;
IS: 'is' ;
FOR: 'for' ;
IN: 'in' ;
WHILE: 'while' ;
WHEN: 'when' ;
ABSTRACT: 'abstract' ;
CLASS: 'class' ;
GET: 'get' ;
INTERFACE: 'interface' ;
OVERRIDE: 'override' ;
NullLiteral: 'null'; 
TYPE: ('Int' | 'Unit' | 'Any' | 'Long' | 'String' | 'Double' | 'Boolean') '?'? ; 
INT: [0-9]+ ;
REAL: [0-9]+'.'[0-9]+ ;
ID: [A-Za-z]+ INT? ;
NEWLINE : [\r\n] ;
WS : [ \t\r]+ -> skip ;

