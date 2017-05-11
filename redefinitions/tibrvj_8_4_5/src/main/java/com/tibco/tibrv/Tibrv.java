package com.tibco.tibrv;

public class Tibrv {

	private static int VERSION_MAJOR = 8;
    private static int VERSION_MINOR = 4;
    private static int VERSION_UPDATE = 5;
    private static String VERSION_STRING = "8.4.5";
 
    private static boolean _valid = false;
    private static boolean _haveLib = false;
    private static boolean _javaOnlyOption = false;
    private static TibrvException _exceptionInLoadLib = null;
    private static boolean _closed = false;
    protected static boolean _closing = false;
    private static Object _tibrvLock = new Object();
    private static int _tibrvOpenCount = 0;
 
    private static TibrvQueue _defQueue = null;
    private static TibrvProcessTransport _procTport = null;
    protected static TibrvImpl _impl = null;
 
    private static TibrvErrorCallback _errorCB = null;
 
    public static int getMajorVersion() {
      return VERSION_MAJOR;
    }
 
    public static int getMinorVersion() {
      return VERSION_MINOR;
    }
 
    public static int getUpdateVersion() {
      return VERSION_UPDATE;
    }
 
    public static String getVersionString() {
      return getVersion();
    }
 
    public static String getVersion() {
      return VERSION_STRING;
    }
 
    public static String getCmVersion() throws TibrvException {
    	
      synchronized (_tibrvLock) {
        if ((_valid) && (_impl != null))
          return _impl.getCmVersion();
      }
      throw new TibrvException(4);
    }
 
    public static String getFtVersion() throws TibrvException {
      synchronized (_tibrvLock) {
        if ((_valid) && (_impl != null))
          return _impl.getFtVersion();
      }
      throw new TibrvException(4);
    }
 
    public static boolean isNativeImpl() {
      if (true == _javaOnlyOption)
        return false;
      return _haveLib;
    }
 
    static TibrvException getLoadLibException() {
      return _exceptionInLoadLib;
    }
 
    public static boolean isValid() {
      return ((_valid) && (_impl != null));
    }
 
    public static void setErrorCallback(TibrvErrorCallback paramTibrvErrorCallback) {
      _errorCB = paramTibrvErrorCallback;
    }
 
    public static TibrvErrorCallback getErrorCallback() {
      return _errorCB;
    }
 
    public static TibrvQueue defaultQueue() {
      return _defQueue;
    }
 
    public static TibrvProcessTransport processTransport() {
      return _procTport;
    }
 
    public static void open() throws TibrvException {
      // open(0);
    }
 
    protected static boolean loadLibIfNeeded() {
      try {
        loadLib();
      }
      catch (TibrvException localTibrvException) {
        return false;
      }
      return true;
    }
 
    public static void open(int paramInt) throws TibrvException {
    	/*
    	synchronized (_tibrvLock) {
        if (_valid) {
          if ((paramInt == 0) || ((paramInt == 2) && (isNativeImpl())) || ((paramInt == 1) && (!(isNativeImpl())))) {
            _tibrvOpenCount += 1;
            return;
          }
          throw new TibrvException("Tibrv already open using different implementation", 27);
        }
  
        switch (paramInt) {
        case 0:
          try {
            loadLib();
 
          }
          catch (TibrvException localTibrvException)
          {
            _haveLib = false;
            _exceptionInLoadLib = localTibrvException;
          }
          break;
 
        case 1:
          _javaOnlyOption = true;
          break;
 
        case 2:
          loadLib();
          break;
 
        default:
          throw new IllegalArgumentException("invalid implementation parameter");
        }
  
        _valid = true;
        try
        {
          initTibrv();
        }
        catch (Throwable localThrowable)
        {
          _closed = false;
          _valid = false;
          _impl = null;
          _defQueue = null;
          _procTport = null;
  
          if (localThrowable instanceof Error) throw ((Error)localThrowable);
          if (localThrowable instanceof RuntimeException) throw ((RuntimeException)localThrowable);
          if (localThrowable instanceof TibrvException) throw ((TibrvException)localThrowable);
          throw new TibrvException("Exception during initialization: " + localThrowable.getClass().getName(), 1, localThrowable);
        }
  
        _closed = false;
        _tibrvOpenCount += 1;
      }
      */
    }
 
    public static void close() throws TibrvException {
      
    	synchronized (_tibrvLock) {
        if (!(_valid)) {
          throw new TibrvException(4);
        }
        if (--_tibrvOpenCount > 0) {
          return;
        }
        _tibrvOpenCount = 0;
  
        _closing = true;
  
        _impl.close();
 
  
        _impl = null;
        _closed = true;
        _closing = false;
        _valid = false;
        _haveLib = false;
        _javaOnlyOption = false;
        _exceptionInLoadLib = null;
        _defQueue = null;
        _procTport = null;
      }
    }
 
 
 
    protected static void checkValid() throws TibrvException {
    	/*
      	if ((!(_valid)) || (_impl == null)) {
        	if ((_closed) || (_closing))
          		throw new TibrvException("Tibrv has been closed", 4);
        	throw new TibrvException("Tibrv not initialized", 4);
      }
      */
    }
 
    protected static void checkValidOrClosing() throws TibrvException {
      if ((!(_valid)) || (_impl == null) || (_closing == true)) {
        if ((_closed) || (_closing))
          throw new TibrvException("Tibrv has been closed", 4);
        throw new TibrvException("Tibrv not initialized", 4);
      }
    }
 
 
 
    private static void initTibrv()
      throws TibrvException
    {
      String str = (isNativeImpl()) ? "C" : "J";
      try {
        _impl = (TibrvImpl)Class.forName("com.tibco.tibrv.TibrvImpl" + str).newInstance();
      }
      catch (ClassNotFoundException localClassNotFoundException) {
        throw new TibrvException(904, localClassNotFoundException);
      }
      catch (Exception localException) {
        throw new TibrvException(1, localException);
 
 
      }
  
      _impl.open(VERSION_MAJOR, VERSION_MINOR, VERSION_UPDATE);
  
      _defQueue = new TibrvQueue(true);
      _procTport = new TibrvProcessTransport();
    }
 
 
 
    protected static Object createObjectImpl(String paramString)
      throws TibrvException
    {
      checkValid();
      String str1 = "";
      String str2 = (isNativeImpl()) ? "C" : "J";
      str2 = "com.tibco.tibrv." + paramString + str2;
      Object localObject = null;
      try
      {
        localObject = Class.forName(str2).newInstance();
      }
      catch (ClassNotFoundException localClassNotFoundException) {
        localObject = null;
        str1 = "Class not found: " + str2;
      }
      catch (InstantiationException localInstantiationException) {
        localObject = null;
        str1 = "InstantiationException while creating " + str2;
      }
      catch (IllegalAccessException localIllegalAccessException) {
        localObject = null;
        str1 = "IllegalAccessException while creating " + str2;
 
      }
  
      if (localObject == null) {
        throw new TibrvException(str1, 1);
      }
      return localObject;
    }
 
 
 
 
 
    private static boolean _loaded = false;
 
 
 
 
    private static void loadLib()
      throws TibrvException
    {
      if (_haveLib) return;
      if (_loaded) {
        _haveLib = true;
        return;
      }
  
      String str1 = "tibrvj";
 
      try
      {
        Class.forName("com.tibco.tibrv.TibrvSdContext");
        str1 = "tibrvjsd";
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
      }
  
      String str2 = System.getProperty("sun.arch.data.model");
      String str3 = System.getProperty("os.arch");
      String str4 = System.getProperty("os.name");
  
      if ((!("OSF1".equals(str4))) && (((!("s390x".equals(str3))) || (!("Linux".equals(str4))))) && 
 
 
        (str2 != null) && ("64".equals(str2)) && (System.getProperty("os.name") != null) && (System.getProperty("os.name").indexOf("Windows") == -1))
 
 
 
      {
        str1 = str1 + "64";
      }
  
      try
      {
        System.loadLibrary(str1);
        initLib();
        _haveLib = true;
        _loaded = true;
      }
      catch (UnsatisfiedLinkError localUnsatisfiedLinkError) {
        throw new TibrvException("Library not found: " + str1, 901, localUnsatisfiedLinkError);
      }
      catch (SecurityException localSecurityException)
      {
        throw new TibrvException("SecurityException occurred while loading library " + str1, 902, localSecurityException);
      }
    }
  
    private static native void initLib()
      throws TibrvException;
  }
