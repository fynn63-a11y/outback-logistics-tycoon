namespace OutbackTycoon;

public enum Category { Electronics, ApparelBasic, ApparelLuxury, Sneakers, CarParts, Fmcg, ColdChain, Hazmat, Bulky }

public enum SiteType { Warehouse = 250000, FcHub = 600000, Depot = 120000, DistributionCenter = 1200000, CrossDock = 400000, ContainerTerminal = 1500000, DataCenter = 900000, AirHub = 700000, HighBay = 800000, ColdStorage = 700000, HazStorage = 900000, GoStore = 300000 }

public enum ServiceLevel { Standard = 0, NextDay = 1, SameDay = 2 }

public class City
{
    public string Name;
    public long Population;
    public double PrimeQuote;
    public City(string name, long pop, double prime) { Name = name; Population = pop; PrimeQuote = prime; }
    public double PopMillions => Population / 1_000_000.0;
}

public class Sku
{
    public string Id, Name;
    public Category Category;
    public double Cost, Price;
    public Sku(string id, string name, Category cat, double cost)
    {
        Id = id; Name = name; Category = cat; Cost = cost;
        Price = Math.Round(cost * 1.4, 2);
    }
}

public class Site
{
    public int Id;
    public SiteType Type;
    public string City;
    public int Level = 1, Stars = 1, BuildDaysLeft = 0;
    public ServiceLevel Service = ServiceLevel.Standard;
    public Dictionary<string, int> Stock = new();
    public double Wear = 0;
    public int PalletSlots => (int)Type * 0 + (int)((long)Type / 250000 * 0) + Type switch
    {
        SiteType.Warehouse => 200, SiteType.FcHub => 500, SiteType.Depot => 150,
        SiteType.DistributionCenter => 800, SiteType.CrossDock => 250, SiteType.ContainerTerminal => 600,
        SiteType.HighBay => 1200, SiteType.ColdStorage => 400, SiteType.HazStorage => 300, _ => 0
    } * Level;
    public bool Operational => BuildDaysLeft <= 0;
    public int PalletUsed { get { int u = 0; foreach (var v in Stock.Values) u += v; return (u + 99) / 100; } }
    public double Throughput(Game g) => Operational ? BaseThroughput() * Level * Stars * (1 - 0.3 * Wear) : 0;
    double BaseThroughput() => Type switch
    {
        SiteType.Warehouse => 100, SiteType.FcHub => 250, SiteType.Depot => 80,
        SiteType.DistributionCenter => 400, SiteType.CrossDock => 150, SiteType.ContainerTerminal => 300,
        SiteType.AirHub => 200, SiteType.HighBay => 150, SiteType.ColdStorage => 120,
        SiteType.HazStorage => 120, SiteType.GoStore => 60, _ => 100
    };
    public double Cost => (long)Type * (1 + 0.8 * Level);
}

public class Tech
{
    public string Name;
    public double RpCost;
    public int MinEra;
    public Tech(string name, double rp, int era) { Name = name; RpCost = rp; MinEra = era; }
}

public class Rival
{
    public string Name;
    public double MarketShare;
    public double Aggression = 1.0;
    public int Sites = 0;
    public Rival(string name, double share, double aggr) { Name = name; MarketShare = share; Aggression = aggr; }
}
