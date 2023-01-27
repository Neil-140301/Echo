const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('path');
const url = require('url');
const { channels } = require('../src/shared/constants');

const express = require('express');
const http = require('http');
const appExpress = express();
const fs = require('fs');

let mainWindow;

function startServer() {

  const server = http.createServer((req, res) => {

    const filePath = path.join("D://Database", req.url);
    fs.stat(filePath, (err, stats) => {
      if (err) {
        res.statusCode = 404;
        res.end(err.message);
        return;
      }
      if (stats.isFile()) {
        fs.createReadStream(filePath).pipe(res);
      } else {
        const files = fs.readdirSync(filePath);
        let response = files.join(', ');
        res.end(response);
        return;
      }
    });

  });

  server.listen(8000);
  console.log('Server running on port 8000');
}

function createWindow() {
  const startUrl = process.env.ELECTRON_START_URL || url.format({
    pathname: path.join(__dirname, '../index.html'),
    protocol: 'file:',
    slashes: true,
  });
  mainWindow = new BrowserWindow({
    width: 800,
    height: 600,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
    },
  });
  mainWindow.loadURL(startUrl);
  mainWindow.on('closed', function () {
    mainWindow = null;
  });

  // start the FTP server
  startServer();

}

app.on('ready', createWindow);

app.on('window-all-closed', function () {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', function () {
  if (mainWindow === null) {
    createWindow();
  }
});

ipcMain.on(channels.APP_INFO, (event) => {
  event.sender.send(channels.APP_INFO, {
    appName: app.getName(),
    appVersion: app.getVersion(),
  });
});
