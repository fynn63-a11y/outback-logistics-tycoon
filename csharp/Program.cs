namespace OutbackTycoon;

public static class Program
{
    public static int Main(string[] args)
    {
        var g = new Game();
        Console.WriteLine("=== Outback Logistics Tycoon (C#-Konsolen-Port) ===");
        Console.Write("Firmenname: ");
        var name = Console.ReadLine();
        if (!string.IsNullOrWhiteSpace(name)) g.CompanyName = name.Trim();
        g.Sandbox = args.Contains("--sandbox");
        
        Console.WriteLine($"Firma '{g.CompanyName}' gegründet. Befehle: status | day [n] | build <typ> | city <name> | tech | research <nr> | ipo | zones | save | load | alerts | quit");
        var rnd = new Random();
        while (!g.GameOver)
        {
            Console.Write("> ");
            var line = Console.ReadLine();
            if (line == null) break;
            var p = line.Trim().Split(' ', StringSplitOptions.RemoveEmptyEntries);
            if (p.Length == 0) continue;
            try
            {
                switch (p[0].ToLowerInvariant())
                {
                    case "quit": case "exit": return 0;
                    case "status":
                        Console.WriteLine($"{g.Date} Tag {g.Day} | Kasse {g.Cash:N0} AUD | Umsatz gestern {g.LastRevenue:N0} | Kosten {g.LastCosts:N0} | RP {g.ResearchPoints:N0} | Moral {g.Morale:N0} | Epoche {g.Era} | PrimoDay in {g.PrimeDayIn} | Rivals {g.RivalShare * 100:N0}% | Standorte {g.Sites.Count}");
                        break;
                    case "day":
                        int n = p.Length > 1 ? int.Parse(p[1]) : 1;
                        for (int i = 0; i < n && !g.GameOver; i++) g.Tick();
                        Console.WriteLine($"→ {g.Date} (Tag {g.Day}), Kasse {g.Cash:N0} AUD");
                        break;
                    case "build":
                        if (p.Length > 1 && Enum.TryParse<SiteType>(p[1], true, out var st)) g.BuildSite(st);
                        else Console.WriteLine("Typen: " + string.Join(", ", Enum.GetNames<SiteType>()));
                        break;
                    case "city":
                        if (p.Length > 1) g.SelectedCity = p[1];
                        Console.WriteLine("Stadt: " + g.SelectedCity + " (" + string.Join(", ", g.Cities.Select(c => c.Name)) + ")");
                        break;
                    case "sites":
                        foreach (var s in g.Sites) Console.WriteLine($"  #{s.Id} {s.Type} in {s.City} | Stufe {s.Level} {s.Service} | {(s.Operational ? $"Durchsatz {s.Throughput(g):N0}/Tag" : "IM BAU")} | Paletten {s.PalletUsed}/{s.PalletSlots} | Bestand {s.Stock.Values.Sum()}");
                        if (g.Sites.Count == 0) Console.WriteLine("  (noch keine Standorte — 'build Warehouse')");
                        break;
                    case "tech":
                        for (int i = 0; i < Game.Techs.Length; i++) { var t = Game.Techs[i]; Console.WriteLine($"  [{i}] {t.Name} ({t.RpCost:N0} RP, Epoche {t.MinEra}+) {(g.Researched.Contains(t) ? "ERFORSCHT" : g.Era < t.MinEra ? "gesperrt" : "")}"); }
                        break;
                    case "research":
                        if (p.Length > 1 && int.TryParse(p[1], out var ti) && ti >= 0 && ti < Game.Techs.Length) g.Research(Game.Techs[ti]);
                        break;
                    case "dev": g.Developers++; break;
                    case "service":
                        if (p.Length > 2 && int.TryParse(p[1], out var si)) { var st = g.Sites.FirstOrDefault(x => x.Id == si); if (st != null && st.Service < ServiceLevel.SameDay) { st.Service++; Alert($"#{si} Service -> {st.Service}"); } }
                        break;
                    case "zone":
                        if (p.Length > 2 && int.TryParse(p[2], out var zi)) g.ZoneAssignment[p[1]] = zi;
                        Console.WriteLine("Zonen: " + string.Join(", ", g.ZoneAssignment.Select(kv => kv.Key + "->#" + kv.Value)));
                        break;
                    case "mkt":
                        if (p.Length > 1) { int l = g.Marketing.GetValueOrDefault(p[1]); if (g.Charge(50_000.0 * (l + 1))) g.Marketing[p[1]] = l + 1; }
                        break;
                    case "fxbuy": g.CnyHoldings += 100_000 / g.FxCny; g.CnyAvgRate = g.FxCny; Alert("CNY gekauft zu " + g.FxCny); break;
                    case "fxsell": { double proceeds = g.CnyHoldings * g.FxCny; g.Cash += proceeds; Alert($"CNY verkauft für {proceeds:N0} AUD (P/L {proceeds - g.CnyHoldings * g.CnyAvgRate:+N0})"); g.CnyHoldings = 0; break; }
                    case "ins": g.InsuranceTier = p.Length > 1 ? int.Parse(p[1]) : 1; break;
                    case "ipo": g.DoIpo(); break;
                    case "alerts": foreach (var a in g.Alerts.Take(15)) Console.WriteLine("  " + a); break;
                    case "sandbox": g.Sandbox = !g.Sandbox; Console.WriteLine("Sandbox: " + g.Sandbox); break;
                    case "save":
                        File.WriteAllText("savegame.txt", $"{g.Day}|{g.Cash}|{g.Reputation}|{g.ResearchPoints}|{g.CompanyName}|{g.IpoDone}|{g.SharePrice}|{string.Join(';', g.Sites.Select(s => $"{s.Id},{s.Type},{s.City},{s.Level},{s.Stars},{s.BuildDaysLeft},{s.Service},{string.Join(',', s.Stock.Select(kv => kv.Key + ':' + kv.Value))}"))}");
                        Console.WriteLine("gespeichert.");
                        break;
                    case "load":
                        if (File.Exists("savegame.txt"))
                        {
                            var parts = File.ReadAllText("savegame.txt").Split('|');
                            g.Day = long.Parse(parts[0]); g.Cash = double.Parse(parts[1]); g.Reputation = double.Parse(parts[2]);
                            g.ResearchPoints = double.Parse(parts[3]); g.CompanyName = parts[4]; g.IpoDone = bool.Parse(parts[5]); g.SharePrice = double.Parse(parts[6]);
                            Console.WriteLine("geladen: " + g.Date);
                        }
                        break;
                    case "help": default:
                        Console.WriteLine("Befehle: status | day [n] | build <typ> | sites | city <name> | tech | research <nr> | dev | ipo | alerts | sandbox | save | load | quit");
                        break;
                }
            }
            catch (Exception ex) { Console.WriteLine("Fehler: " + ex.Message); }
        }
        if (g.GameOver) { Console.WriteLine("GAME OVER: Bankrott!"); return 1; }
        return 0;
    }
}
