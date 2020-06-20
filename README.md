# GpsTest_DFStream
位置情報取得lアプリのDF処理

# 実行方法(windows版)

コマンドプロンプトを開いて、以下のように実行。


```
rem ローカルからDataflowを作成する場合は資格情報が必要。
set GOOGLE_APPLICATION_CREDENTIALS=C:\xxxxx\.json

for /f "usebackq" %%A in (`gcloud config get-value project`) do set PROJECT=%%A
echo %PROJECT%
set BUCKET=%PROJECT%

cd <pom.xmlがあるフォルダ>

mvn compile exec:java -Dexec.mainClass=com.ozbone.gpstest.DfStream -Dexec.args="--project=%PROJECT% --stagingLocation=gs://%BUCKET%/staging/ --gcpTempLocation=gs://%BUCKET%/staging/tmp --runner=DataflowRunner --maxNumWorkers=3"

cd ..

```

以下のように表示されれば、成功。1～2分待ってからGCPコンソールのDataflowに行くと、Streamingモートとして実行されているはず。

```
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  52.596 s
[INFO] Finished at: 2020-06-20T10:27:03+09:00
[INFO] ------------------------------------------------------------------------

```