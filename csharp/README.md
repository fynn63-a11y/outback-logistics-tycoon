# Outback Logistics Tycoon — C#-Port (Konsole)

Läuft auf **Linux, Windows, macOS** mit .NET 8 SDK.

## Linux starten

```bash
# .NET 8 SDK installieren (Ubuntu/Debian):
sudo apt install dotnet-sdk-8.0     # oder: https://dot.net (Dotnet-Install-Skript)

cd csharp
dotnet run            # spielen
# oder Sandbox-Modus (gratis, kein Bankrott):
dotnet run -- --sandbox
```

## Befehle

`status` (Kasse/Datum/RP) · `day [n]` (Tage simulieren, 1 Tag = 5 s Echtzeit im Java-Original) · `build Warehouse` · `sites` · `city Sydney` · `tech` · `research 2` · `dev` (Entwickler) · `ipo` · `alerts` · `save` / `load` · `sandbox` · `quit`

Enthalten: Nachfrage-Modell (Einwohner × Primo × Saison × Epoche × Rivals × Service-Level), Standorte mit Bauzeit/Stufen/Service, ~950 Städte im Java-Original (hier die Top-Städte), ~14 Technologien, Rivals mit Aggressivität, Primo Day, Kredite/Bankrott, IPO + Aktienkurs, Save/Load.

Die grafische Vollversion (Karte, 2.5D-Lager, HoI4-Techbaum) bleibt die Java-Version im Repo-Hauptordner.
