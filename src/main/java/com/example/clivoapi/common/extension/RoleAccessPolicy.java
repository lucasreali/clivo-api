package com.example.clivoapi.common.extension;

public interface RoleAccessPolicy {

    boolean allowsClinicalRecord(ViewerRole viewer);
}
