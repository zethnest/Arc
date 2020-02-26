package arc.fx.util;

import arc.*;
import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.Texture.*;

/**
 * Encapsulates a framebuffer with the ability to ping-pong between two buffers.
 * <p>
 * Upon {@link #begin()} the buffer is reset to a known initial state, this is usually done just before the first usage of the
 * buffer.
 * <p>
 * Subsequent {@link #swap()} calls will initiate writing to the next available buffer, returning the previously used one,
 * effectively ping-ponging between the two. Until {@link #end()} is called, chained rendering will be possible by retrieving the
 * necessary buffers via {@link #getSrcTexture()}, {@link #getSrcBuffer()}, {@link #getDstTexture()} or
 * {@link #getDstBuffer}.
 * <p>
 * When finished, {@link #end()} should be called to stop capturing. When the OpenGL context is lost, {@link #rebind()} should be
 * called.
 * @author bmanuel
 * @author metaphore
 */
public final class PingPongBuffer{
    private final FxBuffer buffer1;
    private final FxBuffer buffer2;

    private FxBuffer bufDst;
    private FxBuffer bufSrc;

    /**
     * Keeps track of the current active buffer.
     * false - first buffer,
     * true - second buffer.
     **/
    private boolean writeState;

    /** Where capturing is started. Should be true between {@link #begin()} and {@link #end()}. */
    private boolean capturing;

    private TextureWrap wrapU = TextureWrap.ClampToEdge;
    private TextureWrap wrapV = TextureWrap.ClampToEdge;
    private TextureFilter filterMin = TextureFilter.Linear;
    private TextureFilter filterMag = TextureFilter.Linear;

    /**
     * Initializes ping-pong buffer with the size of the LibGDX client's area (usually window size).
     * If you use different OpenGL viewport, better use {@link #PingPongBuffer(Format, int, int)}
     * and specify the size manually.
     * @param fbFormat Pixel format of encapsulated {@link FxBuffer}s.
     */
    public PingPongBuffer(Format fbFormat){
        this(fbFormat, Core.graphics.getWidth(), Core.graphics.getHeight());
    }

    /**
     * Initializes ping-pong buffer with the given size.
     * @param fbFormat Pixel format of encapsulated {@link FxBuffer}s.
     */
    public PingPongBuffer(Format fbFormat, int width, int height){
        this.buffer1 = new FxBuffer(fbFormat);
        this.buffer2 = new FxBuffer(fbFormat);
        resize(width, height);

        // Setup src/dst buffers.
        writeState = false;
        this.bufDst = buffer1;
        this.bufSrc = buffer2;
    }

    public void dispose(){
        buffer1.dispose();
        buffer2.dispose();
    }

    public void resize(int width, int height){
        this.buffer1.initialize(width, height);
        this.buffer2.initialize(width, height);
        rebind();
    }

    /**
     * Restores buffer OpenGL parameters. Could be useful in case of OpenGL context loss.
     */
    public void rebind(){
        // FBOs might be null if the instance wasn't initialized with #resize(int, int) yet.
        if(buffer1.getFbo() != null){
            Texture texture = buffer1.getFbo().getTexture();
            texture.setWrap(wrapU, wrapV);
            texture.setFilter(filterMin, filterMag);
        }
        if(buffer2.getFbo() != null){
            Texture texture = buffer2.getFbo().getTexture();
            texture.setWrap(wrapU, wrapV);
            texture.setFilter(filterMin, filterMag);
        }
    }

    /**
     * Start capturing into the destination buffer.
     * To swap buffers during capturing, call {@link #swap()}.
     * {@link #end()} shall be called after rendering to ping-pong buffer is done.
     */
    public void begin(){
        if(capturing){
            throw new IllegalStateException("Ping pong buffer is already in capturing state.");
        }

        capturing = true;
        bufDst.begin();
    }

    /**
     * Swaps source/target buffers.
     * May be called outside of capturing state.
     */
    public void swap(){
        if(capturing){
            bufDst.end();
        }

        // Swap buffers
        if(writeState){
            bufSrc = buffer1;
            bufDst = buffer2;
        }else{
            bufSrc = buffer2;
            bufDst = buffer1;
        }

        if(capturing){
            bufDst.begin();
        }

        writeState = !writeState;
    }

    /**
     * Finishes ping-ponging. Must be called after {@link #begin()}.
     **/
    public void end(){
        if(!capturing){
            throw new IllegalStateException("Ping pong is not in capturing state. You should call begin() before calling end().");
        }
        bufDst.end();
        capturing = false;
    }

    /** @return the source texture of the current ping-pong chain. */
    public Texture getSrcTexture(){
        return bufSrc.getFbo().getTexture();
    }

    /** @return the source buffer of the current ping-pong chain. */
    public FxBuffer getSrcBuffer(){
        return bufSrc;
    }

    /** @return the result's texture of the latest {@link #swap()}. */
    public Texture getDstTexture(){
        return bufDst.getFbo().getTexture();
    }

    /** @return Returns the result's buffer of the latest {@link #swap()}. */
    public FxBuffer getDstBuffer(){
        return bufDst;
    }

    public void setTextureParams(TextureWrap u, TextureWrap v, TextureFilter min, TextureFilter mag){
        wrapU = u;
        wrapV = v;
        filterMin = min;
        filterMag = mag;
        rebind();
    }

    /** @see FxBuffer#addRenderer(FxBuffer.Renderer) ) */
    public void addRenderer(FxBuffer.Renderer renderer){
        buffer1.addRenderer(renderer);
        buffer2.addRenderer(renderer);
    }

    /** @see FxBuffer#removeRenderer(FxBuffer.Renderer) () */
    public void removeRenderer(FxBuffer.Renderer renderer){
        buffer1.removeRenderer(renderer);
        buffer2.removeRenderer(renderer);
    }

    /** @see FxBuffer#clearRenderers() */
    public void clearRenderers(){
        buffer1.clearRenderers();
        buffer2.clearRenderers();
    }

    /**
     * Cleans up managed {@link FxBuffer}s' with specified color.
     */
    public void clear(Color clearColor){
        clear(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
    }

    /**
     * Cleans up managed {@link FxBuffer}s' with specified color.
     */
    public void clear(float r, float g, float b, float a){
        final boolean wasCapturing = this.capturing;

        if(!wasCapturing){
            begin();
        }

        Gl.clearColor(r, g, b, a);
        Gl.clear(Gl.colorBufferBit);
        swap();
        Gl.clear(Gl.colorBufferBit);

        if(!wasCapturing){
            end();
        }
    }
}
