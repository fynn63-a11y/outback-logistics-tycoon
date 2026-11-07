# Outback Logistics Tycoon

Ein Echtzeit-Logistik-Tycoon in Java (Swing, keine externen Abhängigkeiten): Baue dein eigenes
Amazon-ähnliches Imperium in Australien auf — von der ersten Garage 1994 bis zum Börsengang an
der ASX. Inspiriert von Hearts of Iron 4 (Makro-Karte), GeoPolitik 5 und Production Line
(isometrisches Intralogistik-Grid), mit leichter Chiptune-Musik.

## Start

Voraussetzung: JDK 17 oder neuer.

```bash
./build.sh   # kompiliert nach out/
./run.sh     # startet das Spiel
```

## Projekt anlegen (Setup Schritt für Schritt)

1. **JDK installieren:** JDK 17+ von [Adoptium Temurin](https://adoptium.net) laden und installieren.
   Prüfen mit: `javac -version`
2. **Projekt holen:** Dieses Verzeichnis entpacken (oder das Git-Repo klonen) an einen Ort deiner Wahl,
   z. B. `Dokumente/outback-logistics-tycoon`.
3. **Projektstruktur:**
   ```
   outback-logistics-tycoon/
   ├── build.sh / run.sh / generate-textures.sh
   ├── src/com/outback/tycoon/     (Main, core, model, sim, ui, audio, tools)
   ├── assets/textures/            (generierte PNG-Texturen, siehe unten)
   └── README.md
   ```
4. **Kompilieren & starten:** `./build.sh` (legt `out/` an), dann `./run.sh`.
   Alternativ von Hand: `javac -encoding UTF-8 -d out $(find src -name '*.java')` und
   `java -cp out com.outback.tycoon.Main`.
5. **In eine IDE importieren:**
   - IntelliJ IDEA: `File > New > Project from Existing Sources...` → Projektordner wählen → JDK 17 setzen.
   - VS Code: Extension Pack for Java installieren, Ordner öffnen, `Main.java` starten.
   - Eclipse: `File > Import > Existing Projects into Workspace` → Ordner wählen.
6. **Texturen als PNG-Dateien erzeugen (optional):** `./generate-textures.sh` schreibt
   `land.png`, `water.png`, `floor_a.png`, `floor_b.png`, `metal.png` nach `assets/textures/`.

## Wie die Texturen funktionieren

Es gibt **keine handgezeichneten Bilddateien**. Zur Laufzeit erzeugt `ui/Textures.java`
prozedurale Texturen nach dem Standard-Verfahren (Perlin-/Value-Noise + fBm):

- **Gitter-Noise:** Ein Gitter aus zufälligen Werten (per Seed reproduzierbar, Hash-Funktion).
- **Interpolation:** Weiches smoothstep zwischen den Gitterpunkten → kontinuierliche Werte.
- **Kachelbarkeit (tileable):** Koordinaten werden modulo Periode gerechnet — die Textur
  ist nahtlos wiederholbar.
- **fBm (fractal Brownian Motion):** 3–4 Oktaven, jede Oktave doppelte Frequenz, halbe
  Amplitude (Lacunarity 2.0, Persistence 0.5) → natürliche Detail-Tiefe wie bei Terrain.
- **Farb-Mapping:** Der Noise-Wert 0..1 wird auf eine Zweifarb-Palette gemappt + feines Grain.
- **Export:** `tools/TextureExporter.java` rendert die Texturen mit `ImageIO.write` als echte
  PNG-Dateien nach `assets/textures/` — so kannst du sie auch extern weiterverwenden.
- Die Panels cachen die Texturen als `TexturePaint` (werden nicht pro Frame neu berechnet).

## Spielprinzip

- **Echtzeit:** 1 Ingame-Tag = exakt 5 Realzeit-Sekunden. Start: 1. Januar 1994, endloses Spiel.
- **Bankrott:** Fällt die Kasse unter die Schulden-Grenze (-1.000.000 AUD) → sofort Game Over.
- **Sandbox-Modus:** Schalter im HUD — alle Kosten (Bau, Maschinen, Importe, Übernahmen) sind gratis
  und eine Insolvenz ist ausgeschlossen. Erklärtext steht direkt unter dem Schalter; ideal zum
  risikofreien Ausprobieren aller Systeme.

### Ebenen

1. **Makro (Karte):** Interaktive Karte Australiens mit Sydney, Melbourne, Brisbane, Perth und
   Adelaide (Einwohnerzahlen, Prime-Quote, Saisonfaktor). Klick wählt die Stadt.
2. **Meso (Stadt & Bau):** Baue modular Lager, FC-Hubs, Zwischenlager, Verteilzentren,
   Umschlagzentren, Container-Terminals (senken Hafen-Stau) und AWS-Rechenzentren. Größere
   Ausbaustufen kosten mehr, fassen mehr Paletten. Jeder Standort hat 1–5 Automatisierungs-Sterne.
3. **Mikro (20x20-Grid):** Isometrischer 2.5D-Baukasten pro Standort mit kategorisiertem Packbau
   (Wareneingang, Transport, Lagerung, Kommissionierung, Verpackung, Sortierung, Versand):
   Spawner, Förderbänder, Rollwagen, Regale, Pick-Stationen, Kiva-Roboter, Packtische,
   Kartonaufrichter, Sealer, Wickelmaschine, Strapper, Paketstapler, Paketsortierer,
   Sortierzentrum, Lade-/Export-Rampen. Tech-gated Maschinen (Roboter, Sortierer, Stapler,
   Wickler) schalten sich über den Technologiebaum frei. **Prozesskette:** Eingang → Lagerung →
   Kommissionierung → Verpackung → Versand; eine komplette Kette gibt x1.5 Durchsatz.
4. **Dashboard:** Übersicht über Kasse, Umsatz/Kosten, Durchsatz, Automatisierungsgrad
   (Roboter vs. Menschen in %), Ketten-Status und Lagerbestand pro Standort.

### Systeme

- **Autopilot:** Schalter im HUD für autonomes Rebalancing, Pricing und China-Sourcing.
- **Artikelgruppen:** Electronics, Apparel, FMCG, Bulky — mit eigenen Margen, Retourenquoten und
  Frachtklassen.
- **Private Label:** Gründe eigene Marken (250.000 AUD), finanziere Werbekampagnen und verdränge
  Rivalen.
- **Nachfrage:** Skaliert pro Million Einwohner, Prime-Quote, 12-Monats-Saison-Array (Dezember x2.0)
  und periodischem **Prime Day** (x3.5 Traffic).
- **SCM:** Containerseefracht Shenzhen/Shanghai → Sydney/Melbourne (21 Tage + Hafen-Stau bei
  Port Congestion), begrenzte Paletten-Slots.
- **Marketplace (FBA):** Drittanbieter zahlen Lager- und Kommissioniergebühren.
- **Frachtpartner:** 3 Dienstleister, 5 Gewichtsklassen, Vertragsverhandlungen senken Tarife.
- **Technikbaum:** Forschungspunkte durch Entwickler; Kiva-Roboter, High-Speed-Rollbahnen,
  Smart Slotting AI, AWS Cloud, eigene Flotte, Patentschutz.
- **AWS/Rechenzentren:** Serverkapazität gegen Prime-Day-Crashes.
- **IPO:** Ab 50 Mio. AUD geschätzter Marktkapitalisierung an die ASX Sydney (+25 Mio. AUD).
- **HR & Risiken:** Moral, wilde Streiks, PR-Krisen, saisonale Aushilfen im Dezember,
  Industriespionage durch KI-Konkurrenten.
- **Konkurrenz:** Bis zu 5 KI-Konkurrenten (Tab "Konkurrenz") teilen sich das Nachfrage-Volumen
  mit dir, starten Preiskämpfe und erscheinen im Laufe der Jahre neu. Gute eigene Performance
  drängt ihren Marktanteil zurück; mit genug Kapital kannst du sie einzeln übernehmen.
- **Weltregionen:** ~300 Städte als Datenfile (Top 50 Australien, Top 100 Europa, Top 100 USA,
  Top 50 Kanada; `model/CityData.java`). Australien ist ab Start offen, Europa schaltest du mit
  dem IPO frei, Nordamerika (USA/Kanada) ab 200 Mio. AUD Marktkapitalisierung. Gesperrte Städte
  sind auf der Weltkarte grau markiert, Millionenstädte sind beschriftet.
- **Katastrophen & Wetter:** Buschfeuer, Zyklone und Hochwasser sperren Städte für mehrere Tage
  (rote "!"-Markierung auf der Karte) und verzögern Seefracht. Optionale Versicherung zahlt
  Schadenersatz.
- **Kredite & Zinsen:** Statt sofortigem Game Over kannst du Kredite aufnehmen (+500.000 AUD,
  hebt die Schulden-Grenze an). Auf Negativsaldo fallen 12% Zins p.a. an. Bankrott erst unter
  der (erhöhten) Schulden-Grenze.
- **Produkt-Launches:** Entwickle mit 30 RP + 150.000 AUD neue Produkte für deine Eigenmarken —
  mit deutlich höherer Marge als Handelsware.
- **Gewerkschaft:** Verhandlungen gegen Gebühr erhöhen die Moral und senken das Streikrisiko.
- **Grafik:** Rein prozedural in Java2D (keine Bilddateien): Texturen werden per Zufalls-Noise
  zur Laufzeit erzeugt, die Makro-Karte ist eine Weltkarte mit Textur-Look, das Mikro-Grid ist
  isometrisch-2.5D mit animiertem Warenfluss auf den Rollbahnen und Anzeige der maximalen
  Lagerkapazität (Paletten).
- **Szenarien:** Auswahl beim Start — Endlos-Sandbox, IPO-Rennen (Sieg beim Börsengang),
  Prime-Day-Überleben (erster Prime Day ohne Server-Crash), Monopol (alle Konkurrenten übernehmen).
- **Epochen:** Modem-Ära (1994–99), Breitband-Ära (2000–09), Cloud-Ära (2010–19), KI-Ära (ab 2020).
  Jede Epoche hebt die E-Commerce-Nachfrage an und schaltet Tech-Tiers frei (z. B. AWS Cloud und
  Kiva-Roboter erst ab Cloud-Ära).
- **Flotte & Letzte Meile:** LKW/Transporter pro Stadt (+Durchsatz, -Frachtkosten, +Nachfrage);
  eigener Zustelldienst (Letzte Meile) kostet mehr Fracht, bringt aber Nachfrage und Reputation.
- **Warenwelt:** 9 Kategorien (Elektronik, günstige/Premium-Textilien, Sportschuhe, Autoteile,
  FMCG, Kühlkette, Gefahrgut, Bulky) mit ~20 SKUs. Kühlkette und Gefahrgut verursachen laufende
  Lagerkosten; Retouren werden als Statistik erfasst und kosten Bearbeitungsgebühr.
- **Wirtschaft:** Nach dem IPO simulierter Aktienkurs an der ASX ( reagiert auf Gewinn und
  Reputation), Marktkapitalisierung = Kurs x Aktien.
- **Mitarbeiter-Skills:** Schulungen erhöhen die Skill-Stufe (+5% Durchsatz pro Stufe).
- **AWS WebServices:** Mit AWS Cloud: Webshop x1.15, FBA +20%, passive WebService-Einnahmen.
- **Statistik-Tab:** Verlaufsgraphen für Kasse, Tagesumsatz und Konkurrenz-Marktanteil.
- **Speichern/Laden:** komplettes Spiel via Java-Serialisierung (Buttons im HUD).
- **Sound:** Chiptune-Musik + Soundeffekte (Alert, Kasse bei IPO/Übernahme, Crash bei Bankrott).
