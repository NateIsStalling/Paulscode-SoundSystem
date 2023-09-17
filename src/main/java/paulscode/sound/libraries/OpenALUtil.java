package paulscode.sound.libraries;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memFree;

public class OpenALUtil {

    private static boolean created = false;
    private static long device = -1;
    private static long contextAL = -1;
    private static boolean useTLC;
    private static ALCapabilities caps;

    public static void create() throws Exception {
        // Initialise OpenAL
        if (contextAL == -1) {

            device = alcOpenDevice((ByteBuffer)null);
            ALCCapabilities deviceCaps = ALC.createCapabilities(device);

            IntBuffer attribs = BufferUtils.createIntBuffer(16);
            attribs.put(ALC10.ALC_FREQUENCY);
            attribs.put(44100);
            attribs.put(ALC10.ALC_REFRESH);
            attribs.put(60);
            attribs.put(ALC10.ALC_SYNC);
            attribs.put(0);
            attribs.put(0);
            attribs.flip();

            contextAL = alcCreateContext(device, attribs);
            alcMakeContextCurrent(contextAL);

            useTLC = deviceCaps.ALC_EXT_thread_local_context && alcSetThreadContext(contextAL);
            if (!useTLC) {
                if (!alcMakeContextCurrent(contextAL)) {
                    throw new IllegalStateException();
                }
            }

            caps = AL.createCapabilities(deviceCaps);

            created = true;
        }
    }

    public static boolean isCreated() {
        return created;
    }

    public static void destroy() {
        alcDestroyContext(contextAL);
        alcMakeContextCurrent(NULL);
        if (useTLC) {
            AL.setCurrentThread(null);
        } else {
            AL.setCurrentProcess(null);
        }
        memFree(caps.getAddressBuffer());

        alcDestroyContext(contextAL);
        alcCloseDevice(device);

        contextAL = -1;
        device = -1;
        created = false;
    }
}