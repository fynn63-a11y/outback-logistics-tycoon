namespace OutbackTycoon;

public class Game
{
    public long Day = 0;
    public double Cash = 500_000;
    public double DebtLimit = 1_000_000;
    public double Reputation = 50, Morale = 70;
    public double ResearchPoints = 0;
    public int Developers = 1;
    public int PrimeDayIn = 60, PrimeDayLeft = 0;
    public bool IpoDone = false;
    public double SharePrice = 0;
    public bool GameOver = false;
    public bool Sandbox = false;
    public string CompanyName = "Outback Logistics";
    public string SelectedCity = "Sydney";
    public readonly List<City> Cities = new();
    public readonly List<Site> Sites = new();
    public readonly List<Sku> Catalog = new();
    public readonly List<Rival> Rivals = new();
    public readonly HashSet<Tech> Researched = new();
    public readonly List<string> Alerts = new();
    private int nextSiteId = 1;

    public static readonly Tech[] Techs = new Tech[]
    {
        new("High-Speed-Bänder", 40, 0), new("Kiva-Roboter", 80, 2), new("Smart Slotting AI", 60, 1),
        new("OWS Cloud", 100, 2), new("Eigene Flotte", 120, 1), new("Automatisierungs-Protokolle", 50, 1),
        new("Patentschutz", 70, 1), new("Sortier-KI", 90, 2), new("WMS", 140, 2), new("TMS", 120, 1),
        new("1-Click-Checkout", 60, 1), new("ML-Nachfrageprognose", 160, 3), new("Control Tower", 200, 3),
    };

    public static readonly double[] Season = { 0.9, 0.85, 0.95, 1.0, 1.0, 1.05, 1.05, 1.0, 0.95, 1.0, 1.3, 2.0 };
    static readonly double[] DemandPerMillion = { 8, 25, 6, 10, 2, 60, 20, 4, 3 };

    public Game()
    {
        Cities.Add(new City("Sydney", 5_300_000, 0.32));
        Cities.Add(new City("Melbourne", 5_200_000, 0.28));
        Cities.Add(new City("Brisbane", 2_600_000, 0.22));
        Cities.Add(new City("Perth", 2_100_000, 0.18));
        Cities.Add(new City("Adelaide", 1_400_000, 0.20));
        Cities.Add(new City("Gold Coast", 700_000, 0.2));
        Cities.Add(new City("Newcastle", 500_000, 0.18));
        Cities.Add(new City("Canberra", 460_000, 0.25));
        Catalog.Add(new Sku("el1", "NovaPhone X", Category.Electronics, 320));
        Catalog.Add(new Sku("el2", "SoundBuds Pro", Category.Electronics, 45));
        Catalog.Add(new Sku("tx1", "Basic Tee", Category.ApparelBasic, 6));
        Catalog.Add(new Sku("tx2", "Designer Shirt", Category.ApparelLuxury, 60));
        Catalog.Add(new Sku("sn1", "Runner Pro", Category.Sneakers, 80));
        Catalog.Add(new Sku("fm1", "Outback Muesli", Category.Fmcg, 2.2));
        Catalog.Add(new Sku("cc1", "Bio-Milch", Category.ColdChain, 1.5));
        Catalog.Add(new Sku("bu1", "Loungesessel", Category.Bulky, 140));
        Rivals.Add(new Rival("Apex Logistics", 0.12, 1.6));
        Rivals.Add(new Rival("Southern Star Freight", 0.09, 0.8));
        Rivals.Add(new Rival("Pacific Rim Fulfilment", 0.07, 1.2));
    }

    public DateOnly Date => new DateOnly(1994, 1, 1).AddDays((int)Day);
    public int Month => Date.Month;
    public int Era => Date.Year < 2000 ? 0 : Date.Year < 2010 ? 1 : Date.Year < 2020 ? 2 : 3;
    public bool IsPrimeDay => PrimeDayLeft > 0;
    public double Seasonality => Season[Month - 1];
    public double RivalShare => Rivals.Sum(r => r.MarketShare);
    public double MarketCap => IpoDone ? SharePrice * 1_000_000 : Math.Max(0, LastRevenue * 365) * 8;

    public double LastRevenue, LastCosts;

    public City City(string name) => Cities.FirstOrDefault(c => c.Name == name) ?? Cities[0];
    public List<Site> SitesIn(string city) => Sites.Where(s => s.City == city).ToList();

    public bool Charge(double amount)
    {
        if (Sandbox) return true;
        if (Cash < amount) return false;
        Cash -= amount;
        return true;
    }

    public void Alert(string msg)
    {
        Alerts.Insert(0, $"{Date} (Tag {Day}): {msg}");
        if (Alerts.Count > 60) Alerts.RemoveAt(Alerts.Count - 1);
    }

    public Site BuildSite(SiteType type)
    {
        if (!Charge((long)type)) { Alert($"Nicht genug Kapital für {type}."); return null; }
        var s = new Site { Id = nextSiteId++, Type = type, City = SelectedCity, BuildDaysLeft = 10 };
        Sites.Add(s);
        Alert($"{type} in {SelectedCity} gebaut (Baustelle 10 Tage).");
        return s;
    }

    public void Research(Tech t)
    {
        if (Researched.Contains(t) || ResearchPoints < t.RpCost || Era < t.MinEra) return;
        ResearchPoints -= t.RpCost;
        Researched.Add(t);
        Alert($"Technologie erforscht: {t.Name}");
    }

    public double Demand(City c, Sku sku)
    {
        double base_ = DemandPerMillion[(int)sku.Category] * c.PopMillions;
        double prime = 1 + c.PrimeQuote * 0.5;
        double pd = IsPrimeDay ? 3.5 : 1.0;
        double rep = 0.5 + Reputation / 100;
        double rival = Math.Max(0.35, 1 - RivalShare);
        double era = 1 + Era * 0.35;
        var server = SitesIn(c.Name).FirstOrDefault(s => s.Operational);
        double service = server != null ? (ServiceLevel)server.Service switch { ServiceLevel.SameDay => 1.35, ServiceLevel.NextDay => 1.15, _ => 1.0 } : 0.4;
        return base_ * prime * Seasonality * pd * rep * rival * era * service;
    }

    public void Tick()
    {
        Day++;
        if (PrimeDayLeft > 0) PrimeDayLeft--;
        else if (--PrimeDayIn <= 0) { PrimeDayLeft = 3; PrimeDayIn = 90; Alert("PRIMO DAY! Traffic x3.5 für 3 Tage."); }

        double revenue = 0, costs = 0;
        foreach (var c in Cities)
        {
            foreach (var sku in Catalog)
            {
                double d = Demand(c, sku);
                if (d <= 0) continue;
                var sites = SitesIn(c.Name).Where(s => s.Operational).OrderByDescending(s => s.Throughput(this)).ToList();
                double remaining = d;
                foreach (var s in sites)
                {
                    if (remaining <= 0) break;
                    int have = s.Stock.GetValueOrDefault(sku.Id);
                    if (have <= 0) continue;
                    int take = (int)Math.Min(Math.Min(have, remaining), s.Throughput(this) / Catalog.Count);
                    if (take > 0) { s.Stock[sku.Id] = have - take; revenue += take * sku.Price; costs += take * 2.0; remaining -= take; }
                }
            }
            costs += 300; // Frachtpartner
        }
        foreach (var s in Sites) { costs += (long)s.Type * 0.002 * s.Level; if (s.BuildDaysLeft > 0) s.BuildDaysLeft--; if (s.Operational && s.Throughput(this) > 0) s.Wear = Math.Min(1, s.Wear + 0.002); }
        costs += (10 + Developers) * 90_000.0 / 365.0;

        Cash += revenue - costs;
        LastRevenue = revenue; LastCosts = costs;
        ResearchPoints += Developers * 2;
        Morale += (70 - Morale) * 0.01;
        foreach (var r in Rivals) r.MarketShare = Math.Clamp(r.MarketShare + 0.001 * r.Aggression, 0.02, 0.35);
        if (IpoDone) SharePrice = Math.Max(1, SharePrice * (1 + (revenue - costs) / 1_000_000.0 * 0.05));
        if (!Sandbox && Cash < -DebtLimit) { GameOver = true; Alert("GAME OVER: Bankrott!"); }
    }

    public void DoIpo()
    {
        if (IpoDone || MarketCap < 50_000_000) return;
        IpoDone = true; SharePrice = 25; Cash += 25_000_000;
        Alert("IPO an der ASX! +25 Mio. AUD.");
    }
}
