/*
 * Copyright (c) 2018. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.model;

import android.support.annotation.NonNull;

public class Element implements Comparable<Element>{
    private String name;
    private String id;

    public Element(String n, String id) {
        this.name = n;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(@NonNull Element o) {
        return this.name.compareTo(o.getName());
    }
}
