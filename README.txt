Students: Craig Butler, Chris Bywaletz, Adrian Popescu



Run the following command from the directory containing the readme to compile everything.

----------------------------------------------------------------------------------------
javac ftp/client/FTPClient.java ftp/client/process/ClientProcessor.java ftp/client/net/ClientConnectionFactory.java ftp/common/Commands.java ftp/common/Codes.java ftp/common/util/InputParser.java ftp/common/util/MessageWriter.java ftp/common/process/transaction/FileTransaction.java ftp/common/process/transaction/ReceiveFileTransaction.java ftp/common/process/transaction/SendFileTransaction.java ftp/common/process/transaction/TransactionManager.java ftp/common/process/Processor.java ftp/common/net/ConnectionFactory.java ftp/common/net/ControlConnection.java ftp/common/net/DataConnection.java ftp/common/application/Config.java ftp/common/application/Identity.java ftp/server/FTPServer.java ftp/server/process/ServerProcessor.java ftp/server/net/ServerConnectionFactory.java
----------------------------------------------------------------------------------------

run the following command to start the server
----------------------------------------------------------------------------------------
java ftp.server.FTPServer 2021 2022
----------------------------------------------------------------------------------------

run the following command to start the client
----------------------------------------------------------------------------------------
java ftp.client.FTPClient 127.0.0.1 2021 2022


This project was done in its entirety by Craig Butler, Chris Bywaletz, Adrian Popescu

We hereby state that we have not received unauthorized help of any form.
