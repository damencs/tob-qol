package com.tobqol.config.font;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FontTypes
{
	REGULAR("RS Regular"),
	BOLD("RS Bold"),
	SMALL("RS Small"),
	ARIAL("Arial"),
	CAMBRIA("Cambria"),
	ROCKWELL("Rockwell"),
	SEGOE_UI("Segoe Ui"),
	TIMES_NEW_ROMAN("Times New Roman"),
	VERDANA("Verdana");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
