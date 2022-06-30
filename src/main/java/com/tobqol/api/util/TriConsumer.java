package com.tobqol.api.util;

@FunctionalInterface
public interface TriConsumer<L, M, R>
{
	void accept(L l, M m, R r);
}
