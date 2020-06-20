# GpsTest_DFStream
位置情報取得lアプリのDF処理

# mavenの準備(windows版)

・ダウンロード
以下から、mavenをダウンロードして任意の場所に解凍します。
https://maven.apache.org/download.cgi

・環境変数設定
環境変数に、mavenのパスを追加しておきます。(必須ではないですが、便利かと)
例：C:\Users\xxxxx\Documents\apache-maven-3.6.3-bin\apache-maven-3.6.3\bin

・maven-project作成

```
mkdir <プロジェクトフォルダ>
cd <プロジェクトフォルダ>

::::::::::::::::::::::::::::::::::::::::::::::
:: プロジェクトを作成します(groupIdなどは適宜指定してください)
:: 以下では、あらかじめ用意されたapache-beamのスターターテンプレートを利用して作成します
::::::::::::::::::::::::::::::::::::::::::::::
mvn archetype:generate ^
      -DarchetypeGroupId=org.apache.beam ^
      -DarchetypeArtifactId=beam-sdks-java-maven-archetypes-starter ^
      -DarchetypeVersion=2.20.0 ^
      -DgroupId=xx.xx.xx ^
      -DartifactId=dfstream ^
      -Dversion="1.1" ^
      -DinteractiveMode=false

```

あとは、各ソースを上書きしてパッケージ名など適宜修正してください。
EditorはEclipseが楽でした。


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