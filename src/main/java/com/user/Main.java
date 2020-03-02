package com.user;

import com.user.core.Core;

public class Main {
    public static void main(String[] args) {
        // ....
        switch (args.length) {
            case 3: Core.collect(args[0], args[1], args[2], true); break;
            case 2: Core.collect(null, args[0], args[1], true); break;
            default: Core.collect(null, "database.properties", "selenium.properties", false);
        }
    }
}
