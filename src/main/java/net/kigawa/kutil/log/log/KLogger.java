package net.kigawa.kutil.log.log;

import net.kigawa.kutil.kutil.KutilString;
import net.kigawa.kutil.kutil.file.Extension;
import net.kigawa.kutil.kutil.interfaces.LoggerInterface;
import net.kigawa.kutil.kutil.interfaces.Module;
import net.kigawa.kutil.log.log.fomatter.KFileFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;

public class KLogger extends java.util.logging.Logger implements LoggerInterface, Module
{
    private static final Map<String, KLogger> loggers = new LinkedHashMap<>();
    private final String name;
    private final java.util.logging.Logger parentLogger;
    private final Level logLevel;
    private final File logDir;
    private final List<Handler> handlers = new LinkedList<>();
    private KFileHandler fileHandler;

    public KLogger(String name, java.util.logging.Logger parentLogger, Level logLevel, File logDir, Handler... handlers)
    {
        super(name, null);
        this.name = name;
        this.parentLogger = parentLogger;
        this.logLevel = logLevel;
        this.logDir = logDir;

        Collections.addAll(this.handlers, handlers);
    }

    /**
     * @deprecated
     */
    public static void setLogger(KLogger logger)
    {
    }

    /**
     * @deprecated
     */
    public static KLogger getInstance()
    {
        return null;
    }

    /**
     * @deprecated 2022/2/18
     */
    public void anSyncLog(Log log, Level level)
    {
        log(log, level);
    }

    /**
     * @deprecated 2022/2/18
     */
    public void anSyncLog(Object o, Level level)
    {
        log(o, level);
    }

    @Override
    public void enable()
    {
        Path logDirPath = null;
        if (logDir != null) logDirPath = logDir.toPath();

        if (parentLogger == null) setParent(KLogger.getLogger(""));
        else setParent(parentLogger);

        setLevel(logLevel);

        if (logDirPath != null) {
            logDirPath.toFile().mkdirs();
            StringBuffer logName = KutilString.addYearToDate(new StringBuffer("log"), "-");
            File logFile = new File(logDirPath.toFile(), Extension.LOG.addExtension(logName.toString()));

            try {
                KFileHandler handler = new KFileHandler(logFile.getAbsolutePath(), 1024 * 1024, 1, false);
                handler.setLevel(logLevel);
                addHandler(handler);
                handler.setFormatter(new KFileFormatter());
                fileHandler = handler;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        for (Handler handler : handlers) {
            addHandler(handler);
        }

        if (loggers.containsKey(name)) loggers.get(name).disable();
        loggers.put(name, this);
    }

    @Override
    public synchronized void disable()
    {
        loggers.remove(name);
    }

    @Override
    public void fine(Object... objects)
    {
        log(objects, Level.FINE);
    }

    @Override
    public void warning(Object... objects)
    {
        log(objects, Level.WARNING);
    }

    @Override
    public void severe(Object... objects)
    {
        log(objects, Level.SEVERE);
    }

    @Override
    public void info(Object... objects)
    {
        log(objects, Level.INFO);
    }

    @Override
    public void all(Object... objects)
    {
        log(objects, Level.ALL);
    }

    @Override
    public void finer(Object... objects)
    {
        log(objects, Level.FINER);
    }

    @Override
    public void finest(Object... objects)
    {
        log(objects, Level.FINEST);
    }

    public synchronized void log(Object o, Level level)
    {
        if (o.getClass().isArray()) {
            log(o.toString(), level);
            for (Object o1 : (Object[]) o) {
                log(o1, level);
            }
            return;
        }
        if (o instanceof Throwable) {
            Throwable throwable = (Throwable) o;
            log(throwable.toString(), level);
            log(throwable.getStackTrace(), level);
            log(throwable.getSuppressed(), level);
            return;
        }
        if (o instanceof StackTraceElement) {
            StackTraceElement element = (StackTraceElement) o;
            log("\tat " + element, level);
            return;
        }
        super.log(level, o.toString());
    }

    @Override
    public void log(Level level, String str)
    {
        log(str, level);
    }

    public void removeFileHandler()
    {
        removeHandler(fileHandler);
        fileHandler = null;
    }

    public interface Log
    {
        @Override
        String toString();
    }
}
