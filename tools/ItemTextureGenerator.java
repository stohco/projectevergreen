import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

/**
 * ItemTextureGenerator — generates real 16x16 pixel-art PNG textures for
 * placeholder canon items. Each item gets a hand-designed pixel pattern
 * replacing the 237-byte solid-color stub.
 *
 * Run: JAVA_HOME=/tmp/my-project/.jdks/jdk-17.0.13+11/ java -cp tools ItemTextureGenerator.java
 * Output: src/main/resources/assets/ergenverse/textures/item/*.png
 */
public class ItemTextureGenerator {

    static final String OUT_DIR = "src/main/resources/assets/ergenverse/textures/item";
    static final int SIZE = 16;

    static final int TRANSPARENT = 0x00000000;

    // Spirit stone tiers
    static final int STONE_LOW_1 = 0xFFB8D4E8, STONE_LOW_2 = 0xFF8FB8D8, STONE_LOW_3 = 0xFF6B9BC4;
    static final int STONE_MID_1 = 0xFF6B9BC4, STONE_MID_2 = 0xFF4A7BA8, STONE_MID_3 = 0xFF2D5C80;
    static final int STONE_HIGH_1 = 0xFF2D5C80, STONE_HIGH_2 = 0xFF1A3D60, STONE_HIGH_3 = 0xFF0D2050;
    static final int STONE_HIGHLIGHT = 0xFFE0F0FF;
    static final int IMMORTAL_1 = 0xFFFFF4D0, IMMORTAL_2 = 0xFFFFE070, IMMORTAL_3 = 0xFFD4A020;
    static final int SOUL_1 = 0xFFE0F0FF, SOUL_2 = 0xFFA0C0FF, SOUL_3 = 0xFF6080C0;
    static final int SCALE_1 = 0xFF40D080, SCALE_2 = 0xFF20A060, SCALE_3 = 0xFF107040;
    static final int BLOOD_1 = 0xFFE02020, BLOOD_2 = 0xFFA01010, BLOOD_3 = 0xFF600808;
    static final int GLASS_1 = 0xFFC0D0E0, GLASS_2 = 0xFF8090A0;
    static final int DAO_1 = 0xFF807868, DAO_2 = 0xFF605848, DAO_3 = 0xFF403828;
    static final int RUNE_GOLD = 0xFFFFD040;
    static final int FAN_1 = 0xFFF0F0E0, FAN_2 = 0xFFD0C0A0, FAN_3 = 0xFF806030;
    static final int HELL_1 = 0xFF404040, HELL_2 = 0xFF282828, HELL_3 = 0xFF101010;
    static final int SEAL_RED = 0xFFC02020;
    static final int VERM_1 = 0xFFE04020, VERM_2 = 0xFFA02010, VERM_3 = 0xFF601008;
    static final int GOLD = 0xFFFFC040;
    static final int STARFLAG_1 = 0xFF2A1A4A, STARFLAG_2 = 0xFF1A1030, STARFLAG_3 = 0xFF0A0820;
    static final int STAR_WHITE = 0xFFFFF0A0;
    static final int SOULFLAG_1 = 0xFF1A1A1A, SOULFLAG_2 = 0xFF0A0A0A, SOULFLAG_3 = 0xFF050505;
    static final int SOUL_GREEN = 0xFF40E060;
    static final int FLAME_1 = 0xFFFFE040, FLAME_2 = 0xFFFF8020, FLAME_3 = 0xFFFF2040;
    static final int BONE_1 = 0xFFF0E8D8, BONE_2 = 0xFFD0C8B0, BONE_3 = 0xFFA09878;
    static final int AGOD_1 = 0xFF402020, AGOD_2 = 0xFF281010, AGOD_3 = 0xFF180808;
    static final int RED_GLOW = 0xFFFF3030;
    static final int AZURE_1 = 0xFF40C060, AZURE_2 = 0xFF208040, AZURE_3 = 0xFF105020;
    static final int WHALE_1 = 0xFF60A0E0, WHALE_2 = 0xFF3070B0, WHALE_3 = 0xFF185080;
    static final int LEIJI_1 = 0xFF8040C0, LEIJI_2 = 0xFF502080, LEIJI_3 = 0xFF301050;
    static final int LIGHTNING = 0xFFFFFF80;
    static final int NETHER_1 = 0xFF205030, NETHER_2 = 0xFF103020, NETHER_3 = 0xFF082010;
    static final int TOAD_1 = 0xFF80C040, TOAD_2 = 0xFF508020, TOAD_3 = 0xFF305010;
    static final int TOAD_SPOT = 0xFFFFE040;
    static final int TRIB_1 = 0xFFC060E0, TRIB_2 = 0xFF8030A0, TRIB_3 = 0xFF501060;
    static final int MAT_1 = 0xFFC0A060, MAT_2 = 0xFFA08040, MAT_3 = 0xFF806030;
    static final int KEY_1 = 0xFFFFD060, KEY_2 = 0xFFD4A030, KEY_3 = 0xFFA07820;
    static final int VEIN_1 = 0xFF60E0A0, VEIN_2 = 0xFF30A070, VEIN_3 = 0xFF207050;
    static final int ARMOR_1 = 0xFFC0C0D0, ARMOR_2 = 0xFF9090A0, ARMOR_3 = 0xFF606070;

    static void px(int[] buf, int x, int y, int color) {
        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) buf[y * SIZE + x] = color;
    }

    static void fillRect(int[] buf, int x0, int y0, int x1, int y1, int color) {
        for (int y = y0; y <= y1; y++) for (int x = x0; x <= x1; x++) px(buf, x, y, color);
    }

    static void fillCircle(int[] buf, int cx, int cy, int r, int color) {
        for (int y = -r; y <= r; y++) for (int x = -r; x <= r; x++)
            if (x * x + y * y <= r * r) px(buf, cx + x, cy + y, color);
    }

    static void drawLine(int[] buf, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        while (true) {
            px(buf, x0, y0, color);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx) { err += dx; y0 += sy; }
        }
    }

    static void savePNG(String name, int[] buf) throws Exception {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < SIZE; y++) for (int x = 0; x < SIZE; x++) img.setRGB(x, y, buf[y * SIZE + x]);
        File f = new File(OUT_DIR, name + ".png");
        ImageIO.write(img, "PNG", f);
        System.out.println("  wrote " + name + ".png (" + f.length() + " bytes)");
    }

    static int[] clear() {
        int[] buf = new int[SIZE * SIZE];
        Arrays.fill(buf, TRANSPARENT);
        return buf;
    }

    static void drawGem(int[] buf, int cx, int cy, int r, int c1, int c2, int c3, int highlight) {
        for (int y = -r; y <= r; y++) {
            int width = r - Math.abs(y);
            for (int x = -width; x <= width; x++) {
                int dist = Math.abs(x) + Math.abs(y);
                int color = dist <= r / 3 ? c1 : dist <= r * 2 / 3 ? c2 : c3;
                px(buf, cx + x, cy + y, color);
            }
        }
        px(buf, cx - r / 3, cy - r / 3, highlight);
        px(buf, cx - r / 3 + 1, cy - r / 3, highlight);
        px(buf, cx - r / 3, cy - r / 3 + 1, highlight);
    }

    static void drawVial(int[] buf, int cx, int cy, int l1, int l2, int l3, int g1, int g2) {
        fillRect(buf, cx - 1, cy - 5, cx + 1, cy - 3, g2);
        fillRect(buf, cx - 2, cy - 3, cx + 2, cy + 3, g1);
        fillRect(buf, cx - 2, cy - 1, cx + 2, cy + 3, l2);
        fillRect(buf, cx - 2, cy + 2, cx + 2, cy + 3, l3);
        px(buf, cx - 1, cy, l1); px(buf, cx, cy, l1); px(buf, cx + 1, cy, l1);
        fillRect(buf, cx - 1, cy - 6, cx + 1, cy - 5, 0xFF806030);
        px(buf, cx - 2, cy - 2, g2); px(buf, cx - 2, cy - 1, g2);
    }

    static void drawFlag(int[] buf, int bg1, int bg2, int bg3, int emblem) {
        fillRect(buf, 2, 1, 2, 14, 0xFF604030);
        px(buf, 2, 1, 0xFFC0A060);
        fillRect(buf, 3, 2, 12, 8, bg2);
        fillRect(buf, 3, 2, 12, 2, bg1);
        fillRect(buf, 3, 8, 12, 8, bg3);
        px(buf, 13, 3, bg2); px(buf, 13, 5, bg2); px(buf, 13, 7, bg3);
        if (emblem == STAR_WHITE) {
            int sx = 7, sy = 5;
            px(buf, sx, sy - 2, emblem); px(buf, sx, sy - 1, emblem);
            px(buf, sx - 1, sy, emblem); px(buf, sx, sy, emblem); px(buf, sx + 1, sy, emblem);
            px(buf, sx - 2, sy + 1, emblem); px(buf, sx + 2, sy + 1, emblem);
            px(buf, sx - 1, sy + 2, emblem); px(buf, sx + 1, sy + 2, emblem); px(buf, sx, sy + 2, emblem);
        } else if (emblem == SOUL_GREEN) {
            fillCircle(buf, 7, 5, 2, emblem);
            px(buf, 6, 7, emblem); px(buf, 7, 7, emblem); px(buf, 8, 7, emblem);
            px(buf, 5, 8, emblem); px(buf, 9, 8, emblem);
            px(buf, 6, 4, 0xFF000000); px(buf, 8, 4, 0xFF000000);
        }
    }

    static void drawFan(int[] buf, int c1, int c2, int c3) {
        int px_pivot = 12, py_pivot = 13;
        for (int angle = 180; angle <= 270; angle += 3) {
            double rad = Math.toRadians(angle);
            for (int r = 1; r <= 9; r++) {
                int x = px_pivot + (int)(Math.cos(rad) * r);
                int y = py_pivot + (int)(Math.sin(rad) * r);
                if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) {
                    int color = r <= 3 ? c1 : r <= 6 ? c2 : c3;
                    px(buf, x, y, color);
                }
            }
        }
        fillRect(buf, 11, 12, 13, 14, 0xFF604030);
        px(buf, 12, 14, 0xFFC0A060);
    }

    static void drawSeal(int[] buf, int c1, int c2, int c3, int trim) {
        fillRect(buf, 4, 4, 11, 11, c2);
        fillRect(buf, 4, 4, 11, 4, c1);
        fillRect(buf, 4, 11, 11, 11, c3);
        px(buf, 4, 4, trim); px(buf, 11, 4, trim); px(buf, 4, 11, trim); px(buf, 11, 11, trim);
        fillRect(buf, 7, 7, 8, 8, trim);
        px(buf, 6, 7, trim); px(buf, 9, 7, trim); px(buf, 7, 6, trim); px(buf, 8, 9, trim);
        fillRect(buf, 7, 2, 8, 3, trim);
    }

    static void drawFlame(int[] buf) {
        int[] outer = {
            0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,
            0,0,0,0,0,1,2,2,2,1,0,0,0,0,0,0,
            0,0,0,0,1,2,2,2,2,2,1,0,0,0,0,0,
            0,0,0,0,1,2,3,3,3,2,1,0,0,0,0,0,
            0,0,0,1,2,3,3,3,3,3,2,1,0,0,0,0,
            0,0,0,1,2,3,4,4,4,3,2,1,0,0,0,0,
            0,0,1,2,3,4,4,4,4,4,3,2,1,0,0,0,
            0,0,1,2,3,4,5,5,5,4,3,2,1,0,0,0,
            0,0,1,2,3,4,5,6,5,4,3,2,1,0,0,0,
            0,0,0,1,2,3,4,4,4,3,2,1,0,0,0,0,
            0,0,0,0,1,2,3,3,3,2,1,0,0,0,0,0,
            0,0,0,0,0,1,2,2,2,1,0,0,0,0,0,0,
            0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
        };
        int[] palette = {TRANSPARENT, FLAME_3, 0xFFFF6030, FLAME_2, 0xFFFFA040, FLAME_1, 0xFFFFFF80};
        for (int i = 0; i < outer.length; i++) {
            if (outer[i] > 0) px(buf, i % SIZE, i / SIZE, palette[outer[i]]);
        }
    }

    static void drawBone(int[] buf) {
        fillCircle(buf, 4, 6, 2, BONE_1);
        fillRect(buf, 4, 5, 11, 7, BONE_1);
        fillRect(buf, 4, 5, 11, 5, BONE_2);
        fillCircle(buf, 12, 10, 2, BONE_1);
        fillRect(buf, 8, 9, 12, 11, BONE_1);
        fillRect(buf, 8, 9, 12, 9, BONE_2);
        fillRect(buf, 5, 7, 10, 7, BONE_3);
        fillRect(buf, 8, 11, 11, 11, BONE_3);
    }

    static void drawKey(int[] buf, int c1, int c2, int c3) {
        fillCircle(buf, 4, 4, 3, c2);
        fillCircle(buf, 4, 4, 2, c1);
        px(buf, 4, 4, c3);
        fillRect(buf, 4, 7, 6, 13, c2);
        px(buf, 4, 7, c1); px(buf, 5, 7, c1);
        fillRect(buf, 6, 11, 8, 12, c2);
        fillRect(buf, 6, 13, 7, 13, c2);
        px(buf, 8, 11, c1); px(buf, 7, 13, c1);
    }

    static void drawMat(int[] buf) {
        for (int y = 4; y <= 11; y += 2) {
            fillRect(buf, 2, y, 13, y, MAT_1);
            fillRect(buf, 2, y + 1, 13, y + 1, MAT_2);
        }
        fillRect(buf, 1, 3, 14, 3, MAT_3);
        fillRect(buf, 1, 12, 14, 12, MAT_3);
        fillRect(buf, 1, 3, 1, 12, MAT_3);
        fillRect(buf, 14, 3, 14, 12, MAT_3);
        for (int x = 3; x <= 13; x += 2) {
            px(buf, x, 5, MAT_3);
            px(buf, x + 1, 7, MAT_3);
            px(buf, x, 9, MAT_3);
            px(buf, x + 1, 11, MAT_3);
        }
    }

    static void drawArmor(int[] buf) {
        fillRect(buf, 3, 3, 12, 4, ARMOR_1);
        fillRect(buf, 4, 4, 11, 11, ARMOR_2);
        fillRect(buf, 4, 4, 11, 4, ARMOR_1);
        fillRect(buf, 4, 10, 11, 11, ARMOR_3);
        fillRect(buf, 7, 3, 8, 4, TRANSPARENT);
        fillRect(buf, 7, 5, 8, 9, ARMOR_3);
        px(buf, 5, 6, ARMOR_1); px(buf, 10, 6, ARMOR_1);
        px(buf, 5, 8, ARMOR_1); px(buf, 10, 8, ARMOR_1);
        drawLine(buf, 4, 5, 11, 9, ARMOR_3);
    }

    static void drawCore(int[] buf, int c1, int c2, int c3, int aura, boolean lightning) {
        fillCircle(buf, 8, 8, 5, aura);
        drawGem(buf, 8, 8, 4, c1, c2, c3, 0xFFFFFFFF);
        if (lightning) {
            drawLine(buf, 8, 4, 6, 7, LIGHTNING);
            drawLine(buf, 6, 7, 9, 8, LIGHTNING);
            drawLine(buf, 9, 8, 7, 11, LIGHTNING);
        }
    }

    static void drawScale(int[] buf) {
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                int cx = 2 + col * 3 + (row % 2);
                int cy = 2 + row * 3;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -2; dx <= 2; dx++) {
                        if (dx * dx + dy * dy * 4 <= 4) {
                            int color = dy < 0 ? SCALE_1 : dy == 0 ? SCALE_2 : SCALE_3;
                            px(buf, cx + dx, cy + dy, color);
                        }
                    }
                }
            }
        }
        px(buf, 3, 3, 0xFF80F0A0); px(buf, 8, 3, 0xFF80F0A0);
        px(buf, 6, 6, 0xFF80F0A0); px(buf, 11, 6, 0xFF80F0A0);
    }

    static void drawSoulFragment(int[] buf) {
        fillCircle(buf, 8, 6, 4, SOUL_1);
        fillCircle(buf, 8, 6, 3, SOUL_2);
        fillRect(buf, 5, 9, 11, 11, SOUL_2);
        px(buf, 4, 10, SOUL_3); px(buf, 12, 10, SOUL_3);
        px(buf, 5, 12, SOUL_3); px(buf, 7, 12, SOUL_3); px(buf, 9, 12, SOUL_3); px(buf, 11, 12, SOUL_3);
        fillCircle(buf, 8, 6, 2, SOUL_1);
        px(buf, 6, 5, SOUL_3); px(buf, 10, 5, SOUL_3);
    }

    static void drawDaoFragment(int[] buf) {
        fillRect(buf, 3, 3, 12, 12, DAO_2);
        fillRect(buf, 3, 3, 12, 3, DAO_1);
        fillRect(buf, 3, 12, 12, 12, DAO_3);
        fillRect(buf, 3, 3, 3, 12, DAO_1);
        fillRect(buf, 12, 3, 12, 12, DAO_3);
        drawLine(buf, 5, 3, 7, 6, DAO_3);
        drawLine(buf, 7, 6, 5, 9, DAO_3);
        drawLine(buf, 5, 9, 8, 12, DAO_3);
        drawLine(buf, 10, 3, 8, 7, DAO_3);
        drawLine(buf, 8, 7, 11, 10, DAO_3);
        drawLine(buf, 6, 5, 10, 5, RUNE_GOLD);
        drawLine(buf, 8, 5, 8, 10, RUNE_GOLD);
        drawLine(buf, 6, 10, 10, 10, RUNE_GOLD);
        px(buf, 8, 7, RUNE_GOLD);
    }

    static void drawVeinEssence(int[] buf) {
        drawGem(buf, 8, 8, 5, VEIN_1, VEIN_2, VEIN_3, 0xFFE0FFE0);
        drawLine(buf, 4, 6, 8, 8, VEIN_3);
        drawLine(buf, 8, 8, 12, 6, VEIN_3);
        drawLine(buf, 8, 8, 6, 12, VEIN_3);
        drawLine(buf, 8, 8, 11, 11, VEIN_3);
        px(buf, 8, 8, 0xFFFFFFFF);
        px(buf, 5, 7, VEIN_1); px(buf, 11, 7, VEIN_1);
    }

    static void drawTribFragment(int[] buf) {
        int[] bolt = {8,2, 6,5, 8,5, 5,9, 8,9, 4,14};
        for (int i = 0; i < bolt.length - 2; i += 2)
            drawLine(buf, bolt[i], bolt[i+1], bolt[i+2], bolt[i+3], TRIB_1);
        for (int i = 0; i < bolt.length - 2; i += 2)
            drawLine(buf, bolt[i], bolt[i+1], bolt[i+2], bolt[i+3], TRIB_2);
        drawLine(buf, 8, 2, 6, 5, 0xFFFFFFFF);
        drawLine(buf, 6, 5, 8, 5, 0xFFFFFFFF);
        drawLine(buf, 8, 5, 5, 9, 0xFFFFFFFF);
        drawLine(buf, 5, 9, 8, 9, 0xFFFFFFFF);
        drawLine(buf, 8, 9, 4, 14, 0xFFFFFFFF);
        px(buf, 7, 3, TRIB_1); px(buf, 9, 3, TRIB_1);
        px(buf, 5, 7, TRIB_1); px(buf, 9, 7, TRIB_1);
        px(buf, 6, 11, TRIB_1); px(buf, 9, 12, TRIB_1);
    }

    static void drawToadCore(int[] buf) {
        drawCore(buf, TOAD_1, TOAD_2, TOAD_3, 0xFF40E060, false);
        px(buf, 6, 6, TOAD_SPOT); px(buf, 10, 7, TOAD_SPOT);
        px(buf, 7, 10, TOAD_SPOT); px(buf, 10, 10, TOAD_SPOT);
    }

    static void drawNetherCore(int[] buf) {
        drawCore(buf, NETHER_1, NETHER_2, NETHER_3, 0xFF206030, false);
        drawLine(buf, 8, 5, 7, 8, SOUL_GREEN);
        drawLine(buf, 8, 5, 9, 8, SOUL_GREEN);
        drawLine(buf, 7, 8, 9, 8, SOUL_GREEN);
        px(buf, 8, 6, 0xFFA0FFC0); px(buf, 8, 7, 0xFFA0FFC0);
    }

    static void drawHellStamp(int[] buf) {
        fillRect(buf, 3, 5, 12, 13, HELL_2);
        fillRect(buf, 3, 5, 12, 5, HELL_1);
        fillRect(buf, 3, 13, 12, 13, HELL_3);
        fillRect(buf, 7, 2, 8, 4, HELL_2);
        fillRect(buf, 7, 2, 8, 2, HELL_1);
        fillRect(buf, 6, 7, 9, 7, SEAL_RED);
        fillRect(buf, 7, 7, 8, 11, SEAL_RED);
        px(buf, 6, 9, SEAL_RED); px(buf, 9, 9, SEAL_RED);
        px(buf, 6, 11, SEAL_RED); px(buf, 9, 11, SEAL_RED);
    }

    // ── 3D Flying Sword texture (vertical sword sprite for 3D model UV) ─
    // Texture layout (16x16):
    //   y=0-9: blade (thin vertical strip at x=7-8)
    //   y=10: guard (horizontal strip x=3-12)
    //   y=11-14: handle (vertical strip x=6-8)
    //   y=15: pommel (wider strip x=5-8)
    static void draw3DSword(int[] buf, int blade1, int blade2, int blade3,
                            int guardColor, int handleColor, int pommelColor, int edge) {
        // Blade (y=0-9, x=7-8) — with gradient and center fuller
        for (int y = 0; y <= 9; y++) {
            int color = y < 3 ? blade1 : y < 7 ? blade2 : blade3;
            px(buf, 7, y, color);
            px(buf, 8, y, color);
        }
        // Blade edge highlights
        for (int y = 0; y <= 9; y++) {
            px(buf, 7, y, edge);
        }
        // Blade tip (y=0 — pointed)
        px(buf, 7, 0, blade1);
        px(buf, 8, 0, blade1);
        // Guard (y=10, x=3-12)
        for (int x = 3; x <= 12; x++) {
            px(buf, x, 10, guardColor);
        }
        px(buf, 3, 10, 0xFF000000);
        px(buf, 12, 10, 0xFF000000);
        // Handle (y=11-14, x=6-8)
        for (int y = 11; y <= 14; y++) {
            px(buf, 6, y, handleColor);
            px(buf, 7, y, handleColor);
            px(buf, 8, y, handleColor);
        }
        // Handle wrap pattern
        px(buf, 7, 12, 0xFF000000);
        px(buf, 7, 14, 0xFF000000);
        // Pommel (y=15, x=5-9)
        for (int x = 5; x <= 9; x++) {
            px(buf, x, 15, pommelColor);
        }
        px(buf, 5, 15, 0xFF000000);
        px(buf, 9, 15, 0xFF000000);
        // Glow on blade for spirit swords
        px(buf, 7, 3, edge);
        px(buf, 8, 3, edge);
        px(buf, 7, 5, edge);
        px(buf, 8, 5, edge);
    }

    public static void main(String[] args) throws Exception {
        new File(OUT_DIR).mkdirs();
        System.out.println("Generating pixel-art item textures to " + OUT_DIR + "/");

        int[] buf;

        buf = clear(); drawGem(buf, 8, 8, 4, STONE_LOW_1, STONE_LOW_2, STONE_LOW_3, STONE_HIGHLIGHT); savePNG("spirit_stone_low", buf);
        buf = clear(); drawGem(buf, 8, 8, 4, STONE_MID_1, STONE_MID_2, STONE_MID_3, STONE_HIGHLIGHT); savePNG("spirit_stone_mid", buf);
        buf = clear(); drawGem(buf, 8, 8, 5, STONE_HIGH_1, STONE_HIGH_2, STONE_HIGH_3, STONE_HIGHLIGHT); fillCircle(buf, 8, 8, 2, 0xFF4080C0); savePNG("spirit_stone_high", buf);
        buf = clear(); drawGem(buf, 8, 8, 5, IMMORTAL_1, IMMORTAL_2, IMMORTAL_3, 0xFFFFFFFF); px(buf, 5, 5, 0xFFFFFFFF); px(buf, 11, 11, 0xFFFFFFFF); px(buf, 11, 5, 0xFFFFFFE0); px(buf, 5, 11, 0xFFFFFFE0); savePNG("immortal_stone", buf);
        buf = clear(); drawSoulFragment(buf); savePNG("soul_fragment", buf);
        buf = clear(); drawScale(buf); savePNG("dragon_scale", buf);
        buf = clear(); drawVial(buf, 8, 8, BLOOD_1, BLOOD_2, BLOOD_3, GLASS_1, GLASS_2); savePNG("blood_essence", buf);
        buf = clear(); drawDaoFragment(buf); savePNG("dao_fragment", buf);
        buf = clear(); drawFan(buf, FAN_1, FAN_2, FAN_3); savePNG("heaven_fan", buf);
        buf = clear(); drawHellStamp(buf); savePNG("eighteen_hell_stamp", buf);
        buf = clear(); drawSeal(buf, VERM_1, VERM_2, VERM_3, GOLD); savePNG("vermilion_emperor_seal", buf);
        buf = clear(); drawFlag(buf, STARFLAG_1, STARFLAG_2, STARFLAG_3, STAR_WHITE); savePNG("star_sealing_flag", buf);
        buf = clear(); drawFlag(buf, SOULFLAG_1, SOULFLAG_2, SOULFLAG_3, SOUL_GREEN); savePNG("soul_refining_flag", buf);
        buf = clear(); drawFlame(buf); savePNG("nine_color_flame", buf);
        buf = clear(); drawBone(buf); savePNG("ancient_god_bone", buf);
        buf = clear(); drawCore(buf, AGOD_1, AGOD_2, AGOD_3, RED_GLOW, false); savePNG("ancient_god_core", buf);
        buf = clear(); drawCore(buf, AZURE_1, AZURE_2, AZURE_3, 0xFF40E060, false); savePNG("azure_dragon_core", buf);
        buf = clear(); drawCore(buf, WHALE_1, WHALE_2, WHALE_3, 0xFF60C0FF, false); drawLine(buf, 5, 8, 7, 7, 0xFFE0F0FF); drawLine(buf, 9, 9, 11, 8, 0xFFE0F0FF); savePNG("cloud_whale_core", buf);
        buf = clear(); drawCore(buf, LEIJI_1, LEIJI_2, LEIJI_3, 0xFFA040E0, true); savePNG("lei_ji_core", buf);
        buf = clear(); drawNetherCore(buf); savePNG("nether_core", buf);
        buf = clear(); drawToadCore(buf); savePNG("thunder_toad_core", buf);
        buf = clear(); drawTribFragment(buf); savePNG("tribulation_fragment", buf);
        buf = clear(); drawMat(buf); savePNG("cultivation_mat", buf);
        buf = clear(); drawKey(buf, KEY_1, KEY_2, KEY_3); savePNG("cave_world_key", buf);
        buf = clear(); drawVeinEssence(buf); savePNG("spirit_vein_essence", buf);
        buf = clear(); drawArmor(buf); savePNG("spirit_armor", buf);

        System.out.println("\nDone! Generated 26 pixel-art item textures.");

        // ── 3D Flying Sword textures ──────────────────────────────────
        System.out.println("\nGenerating 3D flying sword textures...");
        // Wealth Flying Sword — steel/silver blade, bronze guard, brown handle
        buf = clear(); draw3DSword(buf, 0xFFE0E0E0, 0xFFC0C0C0, 0xFF909090, 0xFFD4A030, 0xFF604030, 0xFFC0A040, 0xFFFFFFFF); savePNG("wealth_flying_sword", buf);
        // Core Treasure Sword — golden blade, gold guard, jade handle
        buf = clear(); draw3DSword(buf, 0xFFFFE060, 0xFFD4A030, 0xFFA07820, 0xFFFFD040, 0xFF40C060, 0xFFFFE060, 0xFFFFFFFF); savePNG("core_treasure_sword", buf);
        // Blood Slaughter Sword — dark red blade, dark guard, black handle
        buf = clear(); draw3DSword(buf, 0xFFC02020, 0xFF901010, 0xFF600808, 0xFF400404, 0xFF200202, 0xFF600808, 0xFFFF6060); savePNG("blood_slaughter_sword", buf);
        // Dark Green Flying Sword — green blade, dark green guard, brown handle
        buf = clear(); draw3DSword(buf, 0xFF40C050, 0xFF208030, 0xFF105020, 0xFF206020, 0xFF604030, 0xFF40A040, 0xFF80FF80); savePNG("dark_green_flying_sword", buf);
        // God-Slaying Sword — black/purple blade, dark guard, obsidian handle
        buf = clear(); draw3DSword(buf, 0xFF402060, 0xFF281040, 0xFF180820, 0xFF100818, 0xFF100818, 0xFF6020A0, 0xFFC060E0); savePNG("god_slaying_sword", buf);
        // Blood Refine Sword — crimson blade, red guard, dark handle
        buf = clear(); draw3DSword(buf, 0xFFE04040, 0xFFB02020, 0xFF801010, 0xFF600808, 0xFF403030, 0xFFC02020, 0xFFFF8080); savePNG("blood_refine_sword", buf);
        // Crystal Sword — translucent blue-white blade, silver guard, white handle
        buf = clear(); draw3DSword(buf, 0xFFA0E0FF, 0xFF60B0E0, 0xFF3070A0, 0xFFC0C0D0, 0xFFE0E0F0, 0xFFA0C0E0, 0xFFFFFFFF); savePNG("crystal_sword", buf);
        // Dao Imprint Sword — grey blade with gold runes, bronze guard, brown handle
        buf = clear(); draw3DSword(buf, 0xFF909090, 0xFF707070, 0xFF505050, 0xFFD4A030, 0xFF604030, 0xFFD4A030, 0xFFFFD040); savePNG("dao_imprint_sword", buf);

        System.out.println("\nDone! Generated 8 3D flying sword textures.");

        // ── Soul Bead texture (glowing orb with wisps) ────────────────
        System.out.println("\nGenerating soul bead texture...");
        buf = clear();
        // Bead body — dark purple sphere
        fillCircle(buf, 8, 8, 4, 0xFF4A2070);
        fillCircle(buf, 8, 8, 3, 0xFF6A30A0);
        fillCircle(buf, 7, 7, 2, 0xFF8A50C0);
        // Inner glow
        fillCircle(buf, 7, 7, 1, 0xFFC080FF);
        px(buf, 7, 7, 0xFFFFFFFF);
        // Soul wisps
        px(buf, 4, 5, 0xFFA060E0);
        px(buf, 12, 5, 0xFFA060E0);
        px(buf, 4, 11, 0xFFA060E0);
        px(buf, 12, 11, 0xFFA060E0);
        px(buf, 3, 8, 0xFF8040C0);
        px(buf, 13, 8, 0xFF8040C0);
        savePNG("soul_bead", buf);

        System.out.println("\nDone! Generated soul bead texture.");
    }
}
