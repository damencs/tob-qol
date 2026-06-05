package com.tobqol;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class TestUtils
{
	private TestUtils()
	{
	}

	public static void setField(Object target, String fieldName, Object value)
	{
		Field field = findField(target.getClass(), fieldName);
		field.setAccessible(true);
		try
		{
			field.set(target, value);
		}
		catch (IllegalAccessException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args)
	{
		Method method = findMethod(target.getClass(), methodName, parameterTypes);
		method.setAccessible(true);
		try
		{
			return method.invoke(target, args);
		}
		catch (ReflectiveOperationException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	private static Field findField(Class<?> type, String fieldName)
	{
		Class<?> current = type;
		while (current != null)
		{
			try
			{
				return current.getDeclaredField(fieldName);
			}
			catch (NoSuchFieldException ignored)
			{
				current = current.getSuperclass();
			}
		}

		throw new IllegalArgumentException("Missing field: " + fieldName);
	}

	private static Method findMethod(Class<?> type, String methodName, Class<?>[] parameterTypes)
	{
		Class<?> current = type;
		while (current != null)
		{
			try
			{
				return current.getDeclaredMethod(methodName, parameterTypes);
			}
			catch (NoSuchMethodException ignored)
			{
				current = current.getSuperclass();
			}
		}

		throw new IllegalArgumentException("Missing method: " + methodName);
	}
}
