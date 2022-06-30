package com.tobqol.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupplyChestPreference
{
    OFF("Value"),
    BUY_1("Buy-1"),
    BUY_X("Buy-x"),
    BUY_ALL("Buy-all");

    private final String name;

    @Override
    public String toString()
    {
        return name;
    }
}
