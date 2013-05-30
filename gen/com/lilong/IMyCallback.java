/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\Android\\1.AndroidProject\\MyPlayer\\src\\com\\lilong\\IMyCallback.aidl
 */
package com.lilong;
public interface IMyCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.lilong.IMyCallback
{
private static final java.lang.String DESCRIPTOR = "com.lilong.IMyCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.lilong.IMyCallback interface,
 * generating a proxy if needed.
 */
public static com.lilong.IMyCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.lilong.IMyCallback))) {
return ((com.lilong.IMyCallback)iin);
}
return new com.lilong.IMyCallback.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_newSong:
{
data.enforceInterface(DESCRIPTOR);
this.newSong();
reply.writeNoException();
return true;
}
case TRANSACTION_musicError:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.musicError(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_playingState:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _arg1;
_arg1 = (0!=data.readInt());
this.playingState(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.lilong.IMyCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void newSong() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_newSong, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void musicError(java.lang.String msg) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(msg);
mRemote.transact(Stub.TRANSACTION_musicError, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void playingState(int i, boolean b) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(i);
_data.writeInt(((b)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_playingState, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_newSong = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_musicError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_playingState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void newSong() throws android.os.RemoteException;
public void musicError(java.lang.String msg) throws android.os.RemoteException;
public void playingState(int i, boolean b) throws android.os.RemoteException;
}
