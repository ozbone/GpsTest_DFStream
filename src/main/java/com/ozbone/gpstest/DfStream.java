/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ozbone.gpstest;

import java.util.ArrayList;
import java.util.List;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;

public class DfStream {
	private static final Logger LOG       = LoggerFactory.getLogger(DfStream.class);

	// BigQueryテーブル定義とか
	private static final String outtable  = "location.loc_data";
	private static final String strFields = "id:int64,latitude:float64,longitude:float64,altitude:float64,dt:datetime";

	// -------------------------------------------------
	// カスタムオプション(今回はGoogleのサンプルのまま※特にカスタムオプション無し)
	// -------------------------------------------------
	public interface MyOptions extends DataflowPipelineOptions {
        @Description("Dataflow Streaming Options.")
        @Default.String("DEFAULT")
        String getMyCustomOption();
        void setMyCustomOption(String myCustomOption);
    }

	// -------------------------------------------------
	// Main処理です!
	// -------------------------------------------------
	public static void main(String[] args) {

		// --------------------------
		// 初期設定するよ！
		// --------------------------
		LOG.info("Start Initialize");

		// Dataflowオプションを設定
		MyOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(MyOptions.class);
		options.setStreaming(true); // Steraming Dataflow！
		Pipeline p = Pipeline.create(options);

		String project     = options.getProject(); // プロジェクト名
		String topic = "projects/" + project + "/topics/locationdata";
		TableSchema schema = createSchema(strFields);

		// --------------------------
		// Pipeline開始するよ！
		// --------------------------
		LOG.info("Start PipeLine");
		PCollection<MyLocation> myloc = p //
				.apply("location:pubsub-read",PubsubIO.readStrings().fromTopic(topic)) // PubSubから読み込んで・・
				.apply("location:parse",ParDo.of(new DoFn<String , MyLocation>(){
					@ProcessElement
					public void processElement(ProcessContext c) {
						try {
							LOG.info("Data:" + c.element());
							String line = c.element();
							MyLocation ml = new MyLocation(line.split(","));
							c.output(ml);
						}
						catch (Exception e) {
							LOG.info("pubsub_parse_error" + e.toString());
						}
					}
				}));

		// --------------------------
		// BigQueryに書き込みするよ！
		// --------------------------
		LOG.info("Wrighting-BQ");
		myloc.apply("location:convtoBQROW",ParDo.of(new DoFn<MyLocation,TableRow>(){
			@ProcessElement
			public void processElement(ProcessContext c) {
				MyLocation ml = c.element();
				TableRow row = new TableRow();
				row.set("id", ml.Id);
				row.set("latitude", ml.Latitude);
				row.set("longitude",ml.Longitude);
				row.set("altitude",ml.altitude);
				row.set("dt",ml.dt);
				c.output(row);
			}
		}))
		.apply("location:writeBQ",BigQueryIO.writeTableRows().to(outtable)
				.withSchema(schema)
				.withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND)
				.withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
		);



	    p.run();
	  }

	// -----------------------------------------
	// BigQueryのテーブル情報・・・フィールド名1:データ型1,フィールド名2:データ型2,・・・のように
	// 列挙しているので、それぞれ分解してTableSchemaのリスト型を返却する。
	// mainメソッドから直接呼ぶのでstatic
	// -----------------------------------------
	private static TableSchema createSchema(String schemaText) {
		List<TableFieldSchema> fields = new ArrayList<>();

		// 1列毎にfieldsに追加していく
		for (String fld : schemaText.split(",")) {
			String[] fld_meta = fld.split(":");
			fields.add(new TableFieldSchema().setName(fld_meta[0]).setType(fld_meta[1]));
		}
		TableSchema ts = new TableSchema().setFields(fields);
		return ts;
	}
}
