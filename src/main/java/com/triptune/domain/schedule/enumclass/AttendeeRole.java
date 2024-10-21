package com.triptune.domain.schedule.enumclass;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AttendeeRole {

    AUTHOR(1, "작성자"),
    GUEST(2, "게스트");


    private final int id;
    private final  String role;
}