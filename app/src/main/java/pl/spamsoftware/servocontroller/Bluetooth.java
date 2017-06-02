package pl.spamsoftware.servocontroller;
import android.bluetooth.*;
import android.widget.*;
import android.app.*;
import android.content.*;
import android.os.*;
import java.util.*;
import java.io.*;

public class Bluetooth
{
	BluetoothAdapter mAdapter;
	Handler handler;
	Context context;

	public Bluetooth(Context context)
	{
		this.context = context;
		handler = new Handler(context.getMainLooper());

		mAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mAdapter==null)
			toast("No bluetooth adapter");
		else if(!mAdapter.isEnabled())
			toast("Bluetooth dissabled");
	}

	public BluetoothSocket Accept()
	{
		BluetoothServerSocket server = null;
		try {
			server = mAdapter.listenUsingRfcommWithServiceRecord(
				context.getString(R.string.app_name),
				UUID.fromString("b2b097ed-d105-4cb6-8d03-4809d7efeda4")
			);
		} catch (IOException e) {}

		BluetoothSocket socket = null;
		try {
			socket = server.accept();
			server.close();
		}
		catch (IOException e) {
			return null;
		}
		try {
			server.close();
		} catch (IOException e) {}

		return socket;
	}

	public BluetoothSocket Connect(String name)
	{
		BluetoothSocket socket = null;
		Set<BluetoothDevice>devices = mAdapter.getBondedDevices();
		BluetoothDevice device = null;
		for(BluetoothDevice d : devices)
			if(d.getName().equals(name))
			{
				device = d;
				break;
			}
		if(device == null) {
			toast("No device named '"+name+"'.");
			return null;
		}
		try {
			socket = device.createRfcommSocketToServiceRecord(
				//UUID.fromString("b2b097ed-d105-4cb6-8d03-4809d7efeda4")
				UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
			);
			socket.connect();
		}
		catch (IOException e)
		{
			try {
				socket.close();
			} catch (IOException e2) {}
			return null;
		}
		return socket;
	}

	public void Send(BluetoothSocket socket, byte[] data)
	{
		OutputStream stream = null;
		try
		{
			stream = socket.getOutputStream();
			stream.write(data);
		}
		catch (IOException e)
		{}
	}


	void Recv(BluetoothSocket socket, byte[] data)
	{
		InputStream stream = null;
		try
		{
			stream = socket.getInputStream();
			stream.read(data);

		}
		catch (IOException e)
		{}
	}

	private void toast(final String text)
	{
		handler.post(new Runnable() {
				public void run()
				{
					Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
				}
			});
	}
}
