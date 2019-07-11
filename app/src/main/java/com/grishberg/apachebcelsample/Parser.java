package com.grishberg.apachebcelsample;

import com.github.grishberg.consoleview.Logger;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Parser {
    private static final String TAG = Parser.class.getSimpleName();
    private static final String CONSTRUCTOR = "<init>";
    private ClassGen cgen;
    private ConstantPoolGen pgen;
    private CustomVisitor visitor = new CustomVisitor();
    private final Logger logger;

    public Parser(Logger l) {
        logger = l;
    }

    public void parse(File file, List<File> fileList) throws ParseErrorException {
        ClassParser classParser = new ClassParser(file.getAbsolutePath());
        JavaClass parsedClass;
        try {
            parsedClass = classParser.parse();
            cgen = new ClassGen(parsedClass);
            pgen = cgen.getConstantPool();
            analyzeClass(parsedClass);
        } catch (IOException e) {
            throw new ParseErrorException(e);
        }
    }

    private void analyzeClass(JavaClass parsedClass) {
        // set up the construction tools

        String cname = cgen.getClassName();

        CustomVisitor visitor = new CustomVisitor();
        //walk through methods.
        for (Method method : parsedClass.getMethods()) {

            //вывод информации о текущем методе
            logger.d(TAG, "method: " + method.getName());
            if (!CONSTRUCTOR.equals(method.getName())) {
                // not interesting
                logger.d(TAG, "\tnot interesting..");
                continue;
            }

            MethodGen wrapgen = new MethodGen(method, cname, pgen);
            InstructionList instructionList = wrapgen.getInstructionList();
            for (InstructionHandle h : instructionList) {
                h.accept(visitor);
            }
        }
    }

    private class CustomVisitor extends EmptyVisitor {
        @Override
        public void visitInvokeInstruction(InvokeInstruction o) {
            ConstantPoolGen constantPool = cgen.getConstantPool();
            String className = o.getClassName(constantPool);
            String methodName = o.getMethodName(constantPool);
            if (className.equals("kotlin.jvm.internal.Intrinsics") || className.equals("java.lang.Object")) {
                return;
            }
            if (methodName.equals(CONSTRUCTOR)) {
                // it's ok
                return;
            }
            //TODO: check method
            logger.d(TAG, "\t" + o.getMethodName(constantPool) + ", " + className);
        }
    }
}
