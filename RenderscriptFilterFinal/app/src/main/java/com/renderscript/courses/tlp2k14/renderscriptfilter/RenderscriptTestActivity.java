package com.renderscript.courses.tlp2k14.renderscriptfilter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class RenderscriptTestActivity extends Activity {
    private final String TAG = "Img";

    private RenderScript mRS;
    private Allocation mInPixelsAllocation;
    private Allocation mOutPixelsAllocation;
    private ScriptC_filter filter;

    private Bitmap mBitmapIn;
    private Bitmap mBitmapOut;

    private ImageView mDisplayView;
    private TextView executionJavaResult;
    private TextView executionRSResult;
    private TextView executionRSIResult;


    float f[] = new float[9];
    private ScriptIntrinsicConvolve3x3 filterInstrinsics;

    public void updateDisplay() {
        mDisplayView.invalidate();
    }

    void init() {
        mBitmapIn = loadBitmap(R.drawable.nasa);
        mBitmapOut = mBitmapIn.copy(mBitmapIn.getConfig(), true);


        mDisplayView = (ImageView) findViewById(R.id.display);
        mDisplayView.setImageBitmap(mBitmapOut);
        updateDisplay();

        executionJavaResult = (TextView) findViewById(R.id.executionJavaText);
        executionJavaResult.setText("Result Java: not run");
        executionRSIResult = (TextView) findViewById(R.id.executionRSIText);
        executionRSIResult.setText("Result RSI: not run");
        executionRSResult = (TextView) findViewById(R.id.executionRSText);
        executionRSResult.setText("Result RS: not run");


        f[0] =  0.f;    f[1] = -1.f;    f[2] =  0.f;
        f[3] = -1.f;    f[4] =  5.f;    f[5] = -1.f;
        f[6] =  0.f;    f[7] = -1.f;    f[8] =  0.f;

        initRSContext();

    }

    private void initRSContext() {
        //TODO 1:  Inicializar el contexto renderscript
        mRS = RenderScript.create(this);
        //TODO 3: Crear allocations
        mInPixelsAllocation = Allocation.createFromBitmap(mRS, mBitmapIn);
        mOutPixelsAllocation = Allocation.createFromBitmap(mRS, mBitmapOut);
    }

    void cleanup() {
        //TODO 2: Liberar recursos renderscript
        mInPixelsAllocation.destroy();
        mOutPixelsAllocation.destroy();
        mRS.destroy();

        mBitmapIn = null;
        mBitmapOut = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        init();
    }

    @Override
    protected void onPause() {
        super.onPause();

        cleanup();
    }


    @Override
    protected void onResume() {
        super.onResume();

        init();
    }

    private Bitmap loadBitmap(int resource) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeResource(getResources(), resource, options);
    }

    public void executeTest(View v) {
        // TODO 4: Example
//        ScriptC_example example = new ScriptC_example(mRS);


        // TODO 5: Ejecucion Java
        long timeJava = System.currentTimeMillis();
        javaExecution();
        timeJava = System.currentTimeMillis()-timeJava;
        executionJavaResult.setText("Result Java: " + timeJava + "ms");
        Log.i(TAG, "Result Java: " + timeJava + "ms");

        // TODO 10: Ejecucion Renderscript
        initRS();
        long timeRS = System.currentTimeMillis();
        rsExecution();
        timeRS = System.currentTimeMillis()-timeRS;
        executionRSResult.setText("Result RS: "+timeRS+"ms ----- " + timeJava/timeRS +"X");
        Log.i(TAG, "Result RS: "+timeRS+"ms ----- " + timeJava/timeRS +"X");

        // TODO 15: Ejecucion Renderscript Instrinsics
        initRSInstrinsics();
        timeRS = System.currentTimeMillis();
        rsInstrinsicsExecution();
        timeRS = System.currentTimeMillis()-timeRS;
        executionRSIResult.setText("Result RSI: " + timeRS + "ms ----- " + timeJava / timeRS + "X");
        Log.i(TAG, "Result RSI: "+timeRS+"ms ----- " + timeJava/timeRS +"X");

        updateDisplay();
    }

    private void javaExecution() {
        int pixel;
        int x1, x2;
        int y1, y2;
        float r, g, b, a;
        Color c = new Color();

        int width = mBitmapIn.getWidth();
        int height = mBitmapIn.getHeight();

        int x, y;
        for(x = 0; x < width; x++) {
            for (y = 0; y < height; y++) {

                x1 = Math.max(x-1, 0);
                x2 = Math.min(x+1, width-1);
                y1 = Math.max(y-1, 0);
                y2 = Math.min(y+1, height-1);

                r = g = b = a = 0;
                for (int i = x1; i <= x2; i++) {
                    for (int j = y1; j <= y2; j++) {
                        pixel = mBitmapIn.getPixel(i, j);
                        r += c.red(pixel)   * f[(j-y1)*3+(i-x1)];
                        g += c.green(pixel) * f[(j-y1)*3+(i-x1)];
                        b += c.blue(pixel)  * f[(j-y1)*3+(i-x1)];
                        a += c.alpha(pixel) * f[(j-y1)*3+(i-x1)];
                    }
                }

                r = clamp(r, 0, 255);
                g = clamp(g, 0, 255);
                b = clamp(b, 0, 255);
                a = clamp(a, 0, 255);

                mBitmapOut.setPixel(x, y, c.argb((int)a, (int)r, (int)g, (int)b));
            }
        }
    }


    private float clamp(float value, int min, int max) {
        if (value < min) {
            value = min;
        }
        else if (value > max) {
            value = max;
        }
        return value;
    }


    private void initRS() {
        //TODO 6: Inicializar filterInstrinsics
        filter = new ScriptC_filter(mRS);

        //TODO 7: Llamar a los setter
        filter.set_gHeight(mBitmapIn.getHeight());
        filter.set_gWidth(mBitmapIn.getWidth());
        filter.set_gCoeffs(f);

        filter.set_gIn(mInPixelsAllocation);
    }


    private void rsExecution() {
        // TODO 8: Ejecutar kernel
        filter.forEach_root(mOutPixelsAllocation);

        // TODO 9: Llamar al copyTo
        mOutPixelsAllocation.copyTo(mBitmapOut);
    }


    private void initRSInstrinsics() {
        //TODO 11: Inicializar filterInstrinsics
        filterInstrinsics = ScriptIntrinsicConvolve3x3.create(mRS, Element.U8_4(mRS));

        //TODO 12: Llamar a los setter
        filterInstrinsics.setCoefficients(f);
        filterInstrinsics.setInput(mInPixelsAllocation);
    }


    private void rsInstrinsicsExecution() {
        // TODO 13: Ejecutar kernel
        filterInstrinsics.forEach(mOutPixelsAllocation);

        // TODO 14: Llamar al copyTo
        mOutPixelsAllocation.copyTo(mBitmapOut);
    }
}
