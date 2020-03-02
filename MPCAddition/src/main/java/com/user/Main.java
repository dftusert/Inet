package com.user;
import com.user.core.Core;

public class Main {
    public static void main (String[] args) {
        if(args.length < 1) {
            System.out.println("ERROR: argument config directory is needed");
            return;
        }
        Core.run(args[0]);
    }
}
