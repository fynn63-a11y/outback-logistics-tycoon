using Microsoft.Xna.Framework;

namespace MonoGame2D;

// 2.5D isometrisches Lager (Production-Line-Stil) — laeuft auf Linux via MonoGame/DesktopGL
public class WarehouseGame : Game
{
    GraphicsDeviceManager graphics;
    SpriteBatch spriteBatch;
    Texture2D pixel;

    const int Grid = 20, TileW = 48, TileH = 24;
    int[,] machines = new int[Grid, Grid]; // 0=leer, 1..n = Maschinentyp
    string[] machineNames = { "", "Förderband", "Regal", "Packtisch", "Pick-Station", "Roboter", "Sealer", "Rampe" };
    Color[] machineColors = { Color.Transparent, new Color(150, 150, 160), new Color(160, 120, 70), new Color(120, 220, 120), new Color(60, 170, 90), new Color(255, 150, 40), new Color(250, 120, 120), new Color(80, 220, 220) };
    int selectedMachine = 1;
    float time = 0f;

    public WarehouseGame()
    {
        graphics = new GraphicsDeviceManager(this);
        graphics.PreferredBackBufferWidth = 1280;
        graphics.PreferredBackBufferHeight = 800;
        IsMouseVisible = true;
        Window.Title = "Outback Tycoon — 2.5D Lager (MonoGame, Linux-ok)";
    }

    protected override void LoadContent()
    {
        spriteBatch = new SpriteBatch(GraphicsDevice);
        pixel = new Texture2D(GraphicsDevice, 1, 1);
        pixel.SetData(new[] { Color.White });
    }

    protected override void Update(GameTime gameTime)
    {
        time += (float)gameTime.ElapsedGameTime.TotalSeconds;
        var mouse = Mouse.GetState();
        if (mouse.LeftButton == ButtonState.Pressed)
        {
            var cell = CellAt(mouse.X, mouse.Y);
            if (cell != null) machines[cell.Value.X, cell.Value.Y] = selectedMachine;
        }
        if (Keyboard.GetState().IsKeyDown(Keys.D1)) selectedMachine = 1;
        if (Keyboard.GetState().IsKeyDown(Keys.D2)) selectedMachine = 2;
        if (Keyboard.GetState().IsKeyDown(Keys.D3)) selectedMachine = 3;
        if (Keyboard.GetState().IsKeyDown(Keys.D4)) selectedMachine = 4;
        if (Keyboard.GetState().IsKeyDown(Keys.D5)) selectedMachine = 5;
        if (Keyboard.GetState().IsKeyDown(Keys.D6)) selectedMachine = 6;
        if (Keyboard.GetState().IsKeyDown(Keys.D7)) selectedMachine = 7;
        if (Keyboard.GetState().IsKeyDown(Keys.Escape)) Exit();
        base.Update(gameTime);
    }

    Point? CellAt(int mx, int my)
    {
        int ox = (1280 - Grid * TileW) / 2 + Grid * TileW / 2;
        int oy = 80;
        for (int x = 0; x < Grid; x++)
            for (int y = 0; y < Grid; y++)
            {
                int cx = ox + (x - y) * TileW / 2;
                int cy = oy + (x + y) * TileH / 2;
                if (mx >= cx - TileW / 2 && mx <= cx + TileW / 2 && my >= cy - TileH / 2 && my <= cy + TileH / 2)
                    return new Point(x, y);
            }
        return null;
    }

    void Rect(int x, int y, int w, int h, Color c)
    {
        spriteBatch.Draw(pixel, new Rectangle(x, y, w, h), c);
    }

    void Diamond(int cx, int cy, int w, int h, Color c)
    {
        // Raute als 4 Dreiecke approximiert ueber Linien-Polygon via pixel rows
        for (int i = 0; i < h; i++)
        {
            float t = (float)i / h;
            int half = (int)((1 - Math.Abs(t - 0.5f) * 2) * w / 2);
            Rect(cx - half, cy - h / 2 + i, half * 2, 1, c);
        }
    }

    void Box(int cx, int cy, int w, int h, int lift, Color top, Color side)
    {
        Diamond(cx, cy - lift, w, h, top);
        Rect(cx - w / 2, cy - lift, w, lift, side); //簡易 Seitenflaeche
        Rect(cx - w / 2, cy - lift + lift - h / 2, w, h / 2, side);
    }

    protected override void Draw(GameTime gameTime)
    {
        GraphicsDevice.Clear(new Color(24, 28, 38));
        spriteBatch.Begin();

        int ox = (1280 - Grid * TileW) / 2 + Grid * TileW / 2;
        int oy = 80;

        // Hallenboden + Waende
        Diamond(ox, oy, Grid * TileW, Grid * TileH, new Color(50, 56, 70));
        Rect(ox - Grid * TileW / 2 - 20, oy - 120, 20, 120, new Color(105, 92, 78));
        Rect(ox + Grid * TileW / 2, oy - 120, 20, 120, new Color(88, 76, 62));

        for (int x = 0; x < Grid; x++)
            for (int y = 0; y < Grid; y++)
            {
                int cx = ox + (x - y) * TileW / 2;
                int cy = oy + (x + y) * TileH / 2;
                Diamond(cx, cy, TileW - 2, TileH - 2, (x + y) % 2 == 0 ? new Color(62, 70, 86) : new Color(55, 62, 78));
                int m = machines[x, y];
                if (m > 0)
                {
                    Color c = machineColors[m];
                    Diamond(cx, cy - TileH, TileW * 2 / 3, TileH * 2 / 3, c);
                    Rect(cx - TileW / 3, cy - TileH, TileW * 2 / 3, TileH, c * 0.6f);
                }
                // Pakete auf Förderband
                if (m == 1)
                {
                    double p = (time / 0.5 + x * 3 + y * 5) % 1.0;
                    int px = cx + (int)((p - 0.5) * TileW * 0.6);
                    int py = cy + (int)((p - 0.5) * TileH * 0.3);
                    Rect(px - 2, py - 2, 5, 5, new Color(255, 200, 90));
                }
            }

        base.Draw(gameTime);
        spriteBatch.End();
    }
}

public static class Program
{
    public static void Main() => new WarehouseGame().Run();
}
