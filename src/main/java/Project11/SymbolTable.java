package Project11;

import java.util.*;

public class SymbolTable {

    private Map<String, Symbol> classScope;
    private Map<String, Symbol> subroutineScope;

    private int staticCounter;
    private int fieldCounter;
    private int argCounter;
    private int varCounter;

    private static class Symbol {
        String type, kind;
        int index;

        Symbol(String type, String kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }
    }

    public SymbolTable() {
        classScope = new HashMap<>();
        subroutineScope = new HashMap<>();
        staticCounter = 0;
        fieldCounter = 0;
        argCounter = 0;
        varCounter = 0;
    }

    public void startSubroutine() {
        subroutineScope.clear();
        argCounter = 0;
        varCounter = 0;
    }

    public void define(String name, String type, String kind) {
        if (kind.equals("STATIC")) {
            classScope.put(name, new Symbol(type, kind, staticCounter));
            staticCounter++;
        } else if (kind.equals("FIELD")) {
            classScope.put(name, new Symbol(type, kind, fieldCounter));
            fieldCounter++;
        } else if (kind.equals("ARG")) {
            subroutineScope.put(name, new Symbol(type, kind, argCounter));
            argCounter++;
        } else if (kind.equals("VAR")) {
            subroutineScope.put(name, new Symbol(type, kind, varCounter));
            varCounter++;
        }
    }

    public int varCount(String kind) {
        if (kind.equals("STATIC")) {
            return staticCounter;
        }  else if (kind.equals("FIELD")) {
            return fieldCounter;
        }  else if (kind.equals("ARG")) {
            return argCounter;
        }   else if (kind.equals("VAR")) {
            return varCounter;
        }
        return 0;
    }

    public String kindOf(String name) {
        if (subroutineScope.containsKey(name)) {
            return subroutineScope.get(name).kind;
        } else if (classScope.containsKey(name)) {
            return classScope.get(name).kind;
        }
        return "NONE";
    }

    public String typeOf(String name) {
        if (subroutineScope.containsKey(name)) {
            return subroutineScope.get(name).type;
        } else if (classScope.containsKey(name)) {
            return classScope.get(name).type;
        }
        return null;
    }

    public int indexOf(String name) {
        if (subroutineScope.containsKey(name)) {
            return subroutineScope.get(name).index;
        } else if (classScope.containsKey(name)) {
            return classScope.get(name).index;
        }
        return -1;
    }

}
