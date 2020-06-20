package com.ozbone.gpstest;

import java.io.Serializable;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

@SuppressWarnings("serial")
@DefaultCoder(AvroCoder.class)
public class MyLocation implements Serializable{

	long   Id;         // ID
	double Latitude;   // 緯度
	double Longitude;  // 経度
	double altitude;   // 高度(標高)
	String  dt ;        // 取得日時

	public enum LOC_COLS {
		ID,LATITUDE,LONGITUDE,ALTITUDE,DT;
	}

	// 呼び出し元Pipelineにて、pubsubから受け取った値をカンマ区切りで
	// 配列に変換されたものが入ってくるので、引数も配列で。。
	public MyLocation (String fields[]) {
		this.Id        = getLongField(fields,LOC_COLS.ID);
		this.Latitude  = getDoubleField(fields,LOC_COLS.LATITUDE);
		this.Longitude = getDoubleField(fields,LOC_COLS.LONGITUDE);
		this.altitude  = getDoubleField(fields,LOC_COLS.ALTITUDE);
		this.dt        = getField(fields, LOC_COLS.DT);
	}

	// enumからインデックスを取得(ordinal)してfieldsから値を取得
	private String getField(String [] fields,LOC_COLS fld) {
		return fields[fld.ordinal()];
	}

	// double型にデータ変換したうえで値を返す
	private Double getDoubleField(String [] fields,LOC_COLS fld) {
		return Double.parseDouble(getField(fields,fld));
	}

	// long型にデータ変換したうえで値を返す
	private Long getLongField(String [] fields,LOC_COLS fld) {
		return Long.parseLong(getField(fields,fld));
	}
}
