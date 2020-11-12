package com.example.MethodTypeTwo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private LinearLayout layout = null;
    private TextView touch1 = null;
    private TextView touch2 = null;
    private TextView angle = null;
    private TextView angle_new = null;
    private ImageView grad = null;
    private float x, y, x1, x2, y1, y2, theta,centerX,centerY;
    private float valueOfR1, valueOfB1, valueOfR2, valueOfB2;
    private float err;

    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private BluetoothDevice mBTDevice;
    private Handler mHandler;
    private ConnectedThread mConnectedThread;
    private ConnectThread mConnectThread;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // determine if the device support bluetooth
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBTAdapter == null) {
            System.out.println("Device does not support BlueTooth!");
        }

        // turn on bluetooth if it is not enabled
        if (!mBTAdapter.isEnabled()) {
            Intent btEnableIntent = new Intent(mBTAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btEnableIntent, 1);
        }

        // get bluetooth module device
        mPairedDevices = mBTAdapter.getBondedDevices();
        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice device : mPairedDevices) {
                mBTDevice = device;
            }
        }

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    String readMessage;
                    String R1 = null;
                    String B1 = null;
                    String R2 = null;
                    String B2 = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                        int firstIndex = readMessage.indexOf(",");
                        int secondIndex = readMessage.indexOf(",", firstIndex + 1);
                        int thirdIndex = readMessage.indexOf(",", secondIndex + 1);
                        int fourthIndex = readMessage.indexOf(",", thirdIndex + 1);

                        R1 = readMessage.substring(0, firstIndex);
                        B1 = readMessage.substring(firstIndex + 1, secondIndex);
                        R2 = readMessage.substring(secondIndex + 1, thirdIndex);
                        B2 = readMessage.substring(thirdIndex + 1, fourthIndex);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    valueOfR1 = Float.parseFloat(R1);
                    valueOfB1 = Float.parseFloat(B1);
                    valueOfR2 = Float.parseFloat(R2);
                    valueOfB2 = Float.parseFloat(B2);

                    System.out.print(valueOfR1);
                    System.out.print(",");
                    System.out.print(valueOfB1);
                    System.out.print(",");
                    System.out.print(valueOfR2);
                    System.out.print(",");
                    System.out.print(valueOfB2);
                    System.out.println();
                }
            }
        };

        // create the connection thread
        mConnectThread = new ConnectThread(mBTDevice);
        mConnectThread.start();

        // get views
        layout = (LinearLayout) findViewById(R.id.layout);
        touch1 = (TextView) findViewById(R.id.touch1);
        touch2 = (TextView) findViewById(R.id.touch2);
        angle = (TextView) findViewById(R.id.angle);
        angle_new = (TextView) findViewById(R.id.angle_new);
        grad = (ImageView) findViewById(R.id.grad);

        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int pointerCount = event.getPointerCount();
                for (int i = 0; i < pointerCount; i++) {
                    x = event.getX(i);
                    y = event.getY(i);
                    int id = event.getPointerId(i);
                    String position = "ID: " + id + " x: " + x + " y: " + y;
                    if (id == 0) {
                        touch1.setText(position);
                        x1 = x;
                        y1 = y;
                    } else {
                        touch2.setText(position);
                        x2 = x;
                        y2 = y;
                    }
                }
                if (x1 != x2 || y1 != y2) {
                    theta = (float) Math.toDegrees(Math.atan(Math.abs(y1 - y2) / Math.abs(x1 - x2)));
                    if (x1 > x2 && y1 < y2) {
                        theta = 180 + theta;
                    } else if (x1 < x2 && y1 < y2) {
                        theta = 360 - theta;
                    } else if (x1 < x2 && y1 > y2) {
                        theta = Math.abs(theta);
                    } else if (x1 > x2 && y1 > y2) {
                        theta = 180 - theta;
                    }
                }
                if (x1 == x2 && y1 < y2) {
                    theta = 270;
                } else if (x1 == x2 && y1 > y2) {
                    theta = 90;
                } else if (x1 > x2 && y1 == y2) {
                    theta = 180;
                } else if (x1 < x2 && y1 == y2) {
                    theta = 0;
                }

                centerX = (float) (x1 - grad.getWidth() / 2.0);
                centerY = (float) (y1 - grad.getHeight() / 2.0 - touch1.getHeight() - angle.getHeight());

                angle.setText("Old Angle: " + theta);
                grad.setRotation(-theta);
                grad.setX(centerX);
                grad.setY(centerY);

                err = (float) Math.toDegrees(Math.atan(Math.abs(valueOfR1 - valueOfR2) / Math.abs(valueOfB1 - valueOfB2)));
                System.out.println("------------------");
                System.out.println(valueOfR1);
                System.out.println(valueOfR2);
                System.out.println(valueOfB1);
                System.out.println(valueOfB2);
                System.out.println(theta);
                System.out.println(err);
                System.out.println("------------------");
                float new_theta = 0;
                if (valueOfR1 < valueOfR2) {
                    new_theta = err + theta;
                    if (new_theta > 360) {
                        new_theta = new_theta - 360;
                    }
                    grad.setRotation(-new_theta);
                } else if (valueOfR1 > valueOfR2) {
                    new_theta = theta - err;
                    if (new_theta < 0) {
                        new_theta = 360 + new_theta;
                    }
                    grad.setRotation(-new_theta);
                }
                angle_new.setText("New Angle: " + (new_theta));

                return true;
            }
        });
    }

    // thread used for connecting bluetooth device
    private class ConnectThread extends Thread {
        private final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            try {
                mmDevice = device;
                mmSocket = device.createRfcommSocketToServiceRecord(mUUID);
            } catch (IOException e) {
            }
        }

        public void run() {
            mBTAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    return;
                }
            }
            mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    // thread used for transferring data
    private class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            try {
                mmSocket = socket;
                mmInStream = socket.getInputStream();
                mmOutStream = socket.getOutputStream();
            } catch (IOException e) {
            }
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            int ch;
            while (true) {
                try {
                    bytes = 0;
                    while ((ch = mmInStream.read()) != '\n') {
                        if (ch != -1) {
                            buffer[bytes] = (byte) ch;
                            bytes++;
                        }
                    }
                    buffer[bytes] = (byte) '\n';
                    bytes++;
                    mHandler.obtainMessage(1, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}
