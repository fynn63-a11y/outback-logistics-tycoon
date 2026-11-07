# 2.5D Lager (MonoGame, C#)

Läuft auf **Linux** (.NET 8 + MonoGame DesktopGL, OpenGL).

```bash
sudo apt install dotnet-sdk-8.0 libopenal1 libsdl2-2.0-0   # MonoGame-Runtime-Deps
cd csharp/MonoGame2D
dotnet run
```

Steuerung: **1–7** = Maschinentyp wählen, **Linksklick** ins Iso-Grid = Maschine platzieren, Pakete laufen animiert über Förderbänder, **ESC** = beenden. Isometrische 2.5D-Rendering (Rauten-Boden, extrudierte Maschinen-Boxen, Hallenwände) wie das Java-Lager.
