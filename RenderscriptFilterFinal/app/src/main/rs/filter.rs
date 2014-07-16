#pragma version(1)
#pragma rs java_package_name(com.renderscript.courses.tlp2k14.renderscriptfilter)
#pragma rs_fp_relaxed


int32_t gWidth;
int32_t gHeight;
rs_allocation gIn;

float gCoeffs[9];

uchar4 __attribute__((kernel)) root(uint32_t x, uint32_t y) {
    uint32_t x1 = min((int32_t)x+1, gWidth-1);
    uint32_t x2 = max((int32_t)x-1, 0);
    uint32_t y1 = min((int32_t)y+1, gHeight-1);
    uint32_t y2 = max((int32_t)y-1, 0);

    float4 result;

    float4 p00 = convert_float4(rsGetElementAt_uchar4(gIn, x1, y1));
    float4 p01 = convert_float4(rsGetElementAt_uchar4(gIn, x, y1));
    float4 p02 = convert_float4(rsGetElementAt_uchar4(gIn, x2, y1));
    float4 p10 = convert_float4(rsGetElementAt_uchar4(gIn, x1, y));
    float4 p11 = convert_float4(rsGetElementAt_uchar4(gIn, x, y));
    float4 p12 = convert_float4(rsGetElementAt_uchar4(gIn, x2, y));
    float4 p20 = convert_float4(rsGetElementAt_uchar4(gIn, x1, y2));
    float4 p21 = convert_float4(rsGetElementAt_uchar4(gIn, x, y2));
    float4 p22 = convert_float4(rsGetElementAt_uchar4(gIn, x2, y2));

    result = p00  * gCoeffs[0];
    result += p01 * gCoeffs[1];
    result += p02 * gCoeffs[2];
    result += p10 * gCoeffs[3];
    result += p11 * gCoeffs[4];
    result += p12 * gCoeffs[5];
    result += p20 * gCoeffs[6];
    result += p21 * gCoeffs[7];
    result += p22 * gCoeffs[8];

    result = clamp(result, 0.f, 255.f);
    return convert_uchar4(result);
}

