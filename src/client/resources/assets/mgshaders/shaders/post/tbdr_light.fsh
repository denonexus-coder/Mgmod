#version 320 es

#extension GL_EXT_shader_framebuffer_fetch : require

#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:light.glsl>
#moj_import <minecraft:fog.glsl>

precision highp float;
precision highp int;

layout(location = 0) inout highp vec4 fragColor;

uniform sampler2D MainDepthSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 MainDepthSize;
};

const float PI = 3.14159265358979323846;
const float TWO_PI = 6.28318530717958647692;

float saturate(float x) {
    return clamp(x, 0.0, 1.0);
}

vec3 saturate(vec3 x) {
    return clamp(x, vec3(0.0), vec3(1.0));
}

float linearDepth(float depth) {
    const float nearPlane = 0.05;

    float farPlane = max(
        max(FogRenderDistanceEnd, FogSkyEnd),
        64.0
    );

    float z = depth * 2.0 - 1.0;

    return abs(
        (2.0 * nearPlane * farPlane) /
        (
            farPlane +
            nearPlane -
            z * (farPlane - nearPlane)
        )
    );
}

vec3 screenNormal(vec2 uv, float centerDepth) {
    vec2 texel = 1.0 / MainDepthSize;

    float depthX =
        texture(
            MainDepthSampler,
            uv + vec2(texel.x, 0.0)
        ).r;

    float depthY =
        texture(
            MainDepthSampler,
            uv + vec2(0.0, texel.y)
        ).r;

    float dx = depthX - centerDepth;
    float dy = depthY - centerDepth;

    /*
     * Screen-space reconstruction.
     *
     * The depth differences are deliberately bounded. This keeps
     * the calculation cheap on GE8320 and prevents distant terrain
     * from producing unstable normals.
     */
    float scale = 28.0;

    return normalize(
        vec3(
            -dx * scale,
            -dy * scale,
            1.0
        )
    );
}

float contactShadow(vec2 uv, float depth) {
    vec2 texel = 1.0 / MainDepthSize;

    float d0 = texture(
        MainDepthSampler,
        uv + vec2(-texel.x, 0.0)
    ).r;

    float d1 = texture(
        MainDepthSampler,
        uv + vec2(texel.x, 0.0)
    ).r;

    float d2 = texture(
        MainDepthSampler,
        uv + vec2(0.0, -texel.y)
    ).r;

    float d3 = texture(
        MainDepthSampler,
        uv + vec2(0.0, texel.y)
    ).r;

    float variation =
        abs(d0 - depth) +
        abs(d1 - depth) +
        abs(d2 - depth) +
        abs(d3 - depth);

    return 1.0 - saturate(variation * 20.0) * 0.20;
}

vec3 vibrant(vec3 color) {
    float luminance =
        dot(
            color,
            vec3(
                0.2126,
                0.7152,
                0.0722
            )
        );

    /*
     * Mild saturation boost.
     * Kept below aggressive HDR-style curves because this pass
     * executes on every visible fragment.
     */
    return mix(
        vec3(luminance),
        color,
        1.10
    );
}

void main() {

    /*
     * The main color is read directly from the tile/framebuffer.
     *
     * No color sampler.
     * No intermediate color target.
     * No fullscreen texture copy.
     */
    vec4 color = fragColor;

    float depth = texture(
        MainDepthSampler,
        texCoord
    ).r;

    /*
     * Sky pixels already contain their final sky lighting/fog.
     * Do not modify them.
     */
    if (depth >= 0.99995) {
        return;
    }

    /*
     * Depth-derived surface orientation.
     */
    vec3 normal = screenNormal(
        texCoord,
        depth
    );

    /*
     * Minecraft GameTime:
     *
     * 0.0 -> start of day cycle
     * 0.25 -> ~6000 ticks
     * 0.50 -> ~12000 ticks
     * 0.75 -> ~18000 ticks
     */
    float dayAngle =
        GameTime * TWO_PI;

    float rawSunHeight =
        sin(dayAngle);

    /*
     * Smooth day/night transition.
     */
    float daylight =
        smoothstep(
            -0.18,
            0.16,
            rawSunHeight
        );

    /*
     * Stronger light around the middle of the day.
     */
    float noonStrength =
        smoothstep(
            0.20,
            0.85,
            rawSunHeight
        );

    /*
     * Sunrise / sunset band.
     */
    float horizonSun =
        1.0 -
        smoothstep(
            0.05,
            0.35,
            abs(rawSunHeight)
        );

    /*
     * Rotate the vanilla directional light around the world cycle.
     *
     * Light0/Light1 are still supplied by Minecraft's Lighting UBO.
     * The rotation adds the time-dependent solar component.
     */
    vec3 baseSun =
        normalize(Light0_Direction);

    float ca = cos(dayAngle);
    float sa = sin(dayAngle);

    vec3 rotatedSun = normalize(
        vec3(
            baseSun.x * ca - baseSun.z * sa,
            baseSun.y,
            baseSun.x * sa + baseSun.z * ca
        )
    );

    vec3 sunDirection = normalize(
        vec3(
            rotatedSun.x,
            rotatedSun.y * (0.45 + 0.55 * daylight)
                + rawSunHeight * 0.35,
            rotatedSun.z
        )
    );

    /*
     * Direct sunlight.
     */
    float sunDiffuse =
        max(
            dot(normal, sunDirection),
            0.0
        );

    /*
     * Secondary vanilla fill light.
     */
    float fillDiffuse =
        max(
            dot(
                normal,
                normalize(Light1_Direction)
            ),
            0.0
        );

    /*
     * Ambient never reaches complete black.
     *
     * Night:
     *   ambient = ~0.18
     *
     * Day:
     *   ambient = ~0.28
     */
    float ambient =
        mix(
            0.18,
            0.28,
            daylight
        );

    /*
     * Solar intensity changes continuously throughout the day.
     */
    float directIntensity =
        sunDiffuse *
        (
            0.30 +
            0.55 * daylight +
            0.15 * noonStrength
        );

    /*
     * Fill prevents hard digital-looking black faces.
     */
    float fillIntensity =
        fillDiffuse * 0.12;

    float illumination =
        ambient +
        directIntensity +
        fillIntensity;

    /*
     * Contact shadow from depth discontinuities.
     */
    illumination *=
        contactShadow(
            texCoord,
            depth
        );

    /*
     * Keep dark areas dark but never destroy the base texture.
     */
    illumination =
        clamp(
            illumination,
            0.16,
            1.12
        );

    color.rgb *= illumination;

    /*
     * Small warm solar contribution at sunrise/sunset.
     */
    vec3 warmSun =
        vec3(
            1.00,
            0.42,
            0.12
        );

    float warmAmount =
        horizonSun *
        daylight *
        0.075;

    color.rgb +=
        warmSun *
        warmAmount;

    /*
     * Solar rays.
     *
     * Very cheap screen-space approximation:
     * no ray-marching,
     * no texture,
     * no additional framebuffer.
     */
    vec2 sunScreen =
        vec2(
            0.50 +
            0.40 * cos(dayAngle),
            0.58 +
            0.34 * sin(dayAngle)
        );

    vec2 rayVector =
        texCoord -
        sunScreen;

    float rayDistance =
        length(rayVector);

    float rayMask =
        exp(
            -rayDistance * 4.0
        );

    float rayBands =
        0.5 +
        0.5 *
        cos(
            atan(
                rayVector.y,
                rayVector.x
            ) * 10.0 +
            dayAngle * 2.0
        );

    float rays =
        rayMask *
        rayBands *
        daylight *
        0.035;

    color.rgb +=
        warmSun *
        rays;

    /*
     * Vibrant color response.
     */
    color.rgb =
        vibrant(color.rgb);

    /*
     * Depth-driven fog using the actual Minecraft Fog UBO.
     */
    float distanceFromCamera =
        linearDepth(depth);

    float environmentalFog =
        smoothstep(
            FogEnvironmentalStart,
            max(
                FogEnvironmentalEnd,
                FogEnvironmentalStart + 0.001
            ),
            distanceFromCamera
        );

    float renderDistanceFog =
        smoothstep(
            FogRenderDistanceStart,
            max(
                FogRenderDistanceEnd,
                FogRenderDistanceStart + 0.001
            ),
            distanceFromCamera
        );

    float fogFactor =
        max(
            environmentalFog,
            renderDistanceFog
        );

    /*
     * Keep near geometry essentially untouched.
     */
    fogFactor =
        saturate(fogFactor);

    color.rgb =
        mix(
            color.rgb,
            FogColor.rgb,
            fogFactor * FogColor.a
        );

    /*
     * Preserve the original alpha.
     */
    fragColor =
        vec4(
            saturate(color.rgb),
            color.a
        );
}
