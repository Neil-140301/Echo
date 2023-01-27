package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.ftplet.*
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.usermanager.PasswordEncryptor
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.apache.ftpserver.usermanager.impl.WritePermission
import java.io.IOException


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ftpServer()
    }

}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

fun ftpServer() {
    val serverFactory = FtpServerFactory()
    val factory = ListenerFactory()
    factory.port = 1403
    factory.serverAddress = "172.31.48.101"

    serverFactory.addListener("default", factory.createListener())

    val userManagerFactory = PropertiesUserManagerFactory()
    userManagerFactory.passwordEncryptor = object : PasswordEncryptor {
        //We store clear-text passwords in this example
        override fun encrypt(password: String): String {
            return password
        }

        override fun matches(passwordToCheck: String, storedPassword: String): Boolean {
            return passwordToCheck == storedPassword
        }
    }

    val user = BaseUser()
    user.name = "test"
    user.password = "test"
    user.homeDirectory = "/storage/emulated/0/"

    val authorities: MutableList<Authority> = ArrayList()
    authorities.add(WritePermission())
    user.authorities = authorities
    val um = userManagerFactory.createUserManager()

    try {
        um.save(user)
    } catch (ex1: FtpException) {
        Log.e("Error", ex1.toString())
    }

    serverFactory.userManager = um

    val m: MutableMap<String, Ftplet> = mutableMapOf()
    m["miaFtplet"] = object : Ftplet {
        @Throws(FtpException::class)
        override fun init(ftpletContext: FtpletContext) {
            println("init")
            println("Thread #" + Thread.currentThread().id)
        }

        override fun destroy() {
            println("destroy")
            println("Thread #" + Thread.currentThread().id)
        }

        @Throws(FtpException::class, IOException::class)
        override fun beforeCommand(session: FtpSession, request: FtpRequest): FtpletResult {
            //System.out.println("beforeCommand " + session.getUserArgument() + " : " + session.toString() + " | " + request.getArgument() + " : " + request.getCommand() + " : " + request.getRequestLine())
            //System.out.println("Thread #" + Thread.currentThread().getId())

            //do something
            return FtpletResult.DEFAULT //...or return accordingly
        }

        @Throws(FtpException::class, IOException::class)
        override fun afterCommand(
            session: FtpSession,
            request: FtpRequest,
            reply: FtpReply
        ): FtpletResult {
            //System.out.println("afterCommand " + session.getUserArgument() + " : " + session.toString() + " | " + request.getArgument() + " : " + request.getCommand() + " : " + request.getRequestLine() + " | " + reply.getMessage() + " : " + reply.toString())
            //System.out.println("Thread #" + Thread.currentThread().getId())

            //do something
            return FtpletResult.DEFAULT //...or return accordingly
        }

        @Throws(FtpException::class, IOException::class)
        override fun onConnect(session: FtpSession): FtpletResult {
            println("onConnect " + session.userArgument + " : " + session.toString())
            println("Thread #" + Thread.currentThread().id)

//            do something
            return FtpletResult.DEFAULT //...or return accordingly
        }

        @Throws(FtpException::class, IOException::class)
        override fun onDisconnect(session: FtpSession): FtpletResult {
            //System.out.println("onDisconnect " + session.getUserArgument() + " : " + session.toString())
            //System.out.println("Thread #" + Thread.currentThread().getId())

            //do something
            return FtpletResult.DEFAULT //...or return accordingly
        }
    }
    serverFactory.ftplets = m

    Log.d(
        "FTP",
        serverFactory.fileSystem.createFileSystemView(user).getFile("Nil.pdf").size.toString()
    )
    Log.d("FTP", serverFactory.userManager.getUserByName("test").homeDirectory)

    val server = serverFactory.createServer()
    try {
        server.start()
    } catch (ex: FtpException) {
        Log.e("Error", ex.toString())
    }
}