package com.outback.tycoon.model;

public class CityData {
    public static final String[] AUSTRALIEN = {
            "Sydney|5.3", "Melbourne|5.2", "Brisbane|2.6", "Perth|2.1", "Adelaide|1.4",
            "Gold Coast|0.70", "Newcastle|0.50", "Canberra|0.46", "Sunshine Coast|0.35", "Wollongong|0.30",
            "Geelong|0.27", "Hobart|0.25", "Townsville|0.18", "Cairns|0.15", "Darwin|0.15",
            "Toowoomba|0.14", "Mackay|0.12", "Ballarat|0.11", "Bendigo|0.10", "Coffs Harbour|0.075",
            "Bundaberg|0.070", "Tamworth|0.065", "Wagga Wagga|0.065", "Hervey Bay|0.060", "Shepparton|0.050",
            "Port Macquarie|0.050", "Albury|0.055", "Mildura|0.055", "Rockhampton|0.080", "Launceston|0.090",
            "Gladstone|0.045", "Dubbo|0.040", "Geraldton|0.040", "Kalgoorlie|0.030", "Mount Gambier|0.030",
            "Traralgon|0.030", "Alice Springs|0.028", "Burnie|0.020", "Devonport|0.025", "Karratha|0.020",
            "Mount Isa|0.020", "Whyalla|0.022", "Port Lincoln|0.015", "Emerald|0.015", "Broome|0.015",
            "Katherine|0.010", "Weipa|0.004", "Kununurra|0.005", "Nhulunbuy|0.004", "Thursday Island|0.004"
    };

    public static final String[] EUROPA = {
            "London|9.0", "Paris|2.2", "Madrid|3.3", "Barcelona|1.6", "Berlin|3.7", "Hamburg|1.9",
            "Munich|1.5", "Cologne|1.1", "Frankfurt|0.76", "Stuttgart|0.63", "Düsseldorf|0.65", "Leipzig|0.60",
            "Dortmund|0.59", "Essen|0.58", "Bremen|0.57", "Dresden|0.56", "Hannover|0.53", "Nuremberg|0.52",
            "Duisburg|0.50", "Bochum|0.36", "Wuppertal|0.36", "Bonn|0.33", "Münster|0.32", "Karlsruhe|0.31",
            "Mannheim|0.31", "Augsburg|0.30", "Wiesbaden|0.28", "Mönchengladbach|0.27", "Gelsenkirchen|0.26",
            "Aachen|0.25", "Braunschweig|0.25", "Kiel|0.25", "Chemnitz|0.24", "Magdeburg|0.24", "Halle|0.23",
            "Freiburg|0.23", "Mainz|0.22", "Lübeck|0.22", "Rostock|0.21", "Erfurt|0.21", "Kassel|0.20",
            "Amsterdam|0.90", "Rotterdam|0.66", "The Hague|0.55", "Utrecht|0.36", "Eindhoven|0.24",
            "Milan|1.40", "Rome|2.80", "Naples|0.96", "Turin|0.87", "Palermo|0.65", "Genoa|0.58",
            "Bologna|0.39", "Florence|0.37", "Catania|0.31", "Venice|0.26", "Verona|0.26",
            "Vienna|1.90", "Prague|1.30", "Warsaw|1.80", "Budapest|1.70", "Stockholm|1.00",
            "Copenhagen|0.80", "Oslo|0.70", "Helsinki|0.66", "Dublin|0.59", "Lisbon|0.55",
            "Porto|0.23", "Valencia|0.79", "Seville|0.69", "Zaragoza|0.68", "Málaga|0.58",
            "Athens|0.66", "Thessaloniki|0.79", "Brussels|1.20", "Zurich|0.43", "Geneva|0.20",
            "Basel|0.20", "Bern|0.13", "Luxembourg|0.13", "Bratislava|0.42", "Riga|0.63",
            "Vilnius|0.59", "Tallinn|0.45", "Ljubljana|0.29", "Zagreb|0.79", "Belgrade|1.20",
            "Sarajevo|0.28", "Skopje|0.55", "Sofia|1.24", "Bucharest|1.80", "Kyiv|2.90",
            "Gdańsk|0.49", "Kraków|0.78", "Łódź|0.68", "Wrocław|0.67", "Poznań|0.54",
            "Trondheim|0.21", "Bergen|0.29", "Malmö|0.35", "Gothenburg|0.58", "Uppsala|0.23",
            "Aarhus|0.28", "Odense|0.18", "Esbjerg|0.12", "Cardiff|0.37", "Edinburgh|0.52",
            "Glasgow|0.63", "Manchester|0.55", "Birmingham|1.14", "Leeds|0.81", "Sheffield|0.56",
            "Bristol|0.47", "Nottingham|0.33", "Leicester|0.36", "Portsmouth|0.21", "Southampton|0.25"
    };

    public static final String[] USA = {
            "New York|8.5", "Los Angeles|4.0", "Chicago|2.7", "Dallas|2.6", "Houston|2.3",
            "Washington|0.70", "Philadelphia|1.6", "San Antonio|1.5", "San Diego|1.4", "San Jose|1.0",
            "Austin|0.98", "Jacksonville|0.95", "Fort Worth|0.92", "Columbus|0.92", "Charlotte|0.87",
            "San Francisco|0.87", "Indianapolis|0.88", "Seattle|0.75", "Denver|0.72", "Nashville|0.69",
            "Oklahoma City|0.68", "Portland|0.65", "Las Vegas|0.64", "Memphis|0.65", "Louisville|0.62",
            "Baltimore|0.60", "Milwaukee|0.59", "Albuquerque|0.56", "Tucson|0.55", "Fresno|0.54",
            "Sacramento|0.52", "Kansas City|0.51", "Mesa|0.50", "Atlanta|0.50", "Omaha|0.48",
            "Colorado Springs|0.48", "Raleigh|0.47", "Miami|0.47", "Long Beach|0.46", "Virginia Beach|0.45",
            "Oakland|0.43", "Minneapolis|0.43", "Tulsa|0.40", "Arlington|0.40", "New Orleans|0.39",
            "Wichita|0.40", "Cleveland|0.38", "Bakersfield|0.38", "Tampa|0.38", "Aurora|0.38",
            "Honolulu|0.35", "Anaheim|0.35", "Santa Ana|0.33", "St. Louis|0.32", "Riverside|0.32",
            "Corpus Christi|0.32", "Pittsburgh|0.30", "Lexington|0.32", "Anchorage|0.29", "Cincinnati|0.31",
            "Henderson|0.31", "Plano|0.29", "Stockton|0.31", "Toledo|0.29", "Orlando|0.31",
            "Chula Vista|0.28", "Fort Wayne|0.28", "Buffalo|0.28", "Jersey City|0.27", "Durham|0.27",
            "Madison|0.26", "Lubbock|0.26", "Winston-Salem|0.26", "Garland|0.25", "Glendale|0.25",
            "Hialeah|0.24", "Reno|0.25", "Chandler|0.26", "Norfolk|0.24", "Scottsdale|0.26",
            "Irving|0.26", "Fremont|0.23", "Richmond|0.23", "Boise|0.23", "Spokane|0.23",
            "Baton Rouge|0.23", "Des Moines|0.21", "Tacoma|0.22", "San Bernardino|0.22", "Modesto|0.22",
            "Fontana|0.21", "Shreveport|0.20", "Oxnard|0.20", "Little Rock|0.20", "Mobile|0.19"
    };

    public static final String[] KANADA = {
            "Toronto|3.0", "Montreal|2.1", "Vancouver|0.70", "Calgary|1.3", "Edmonton|1.0",
            "Ottawa|1.0", "Winnipeg|0.75", "Quebec City|0.55", "Hamilton|0.57", "Kitchener|0.50",
            "London|0.40", "Victoria|0.37", "Halifax|0.33", "Oshawa|0.34", "Windsor|0.22",
            "Saskatoon|0.27", "Regina|0.23", "St. John's|0.18", "Kelowna|0.18", "Barrie|0.20",
            "Sherbrooke|0.17", "Guelph|0.13", "Abbotsford|0.15", "Kingston|0.13", "Trois-Rivières|0.16",
            "Thunder Bay|0.12", "Sudbury|0.17", "Peterborough|0.08", "Belleville|0.10", "Lethbridge|0.10",
            "Red Deer|0.10", "Kamloops|0.09", "Nanaimo|0.10", "Prince George|0.08", "Medicine Hat|0.06",
            "Grande Prairie|0.07", "Airdrie|0.07", "Sarnia|0.09", "Drummondville|0.08", "Saint-Jérôme|0.08",
            "Granby|0.07", "Chatham|0.04", "Lloydminster|0.03", "Moose Jaw|0.03", "Cornwall|0.05",
            "North Bay|0.05", "Brantford|0.10", "Clarington|0.10", "Ajax|0.13", "Milton|0.13",
            "Pickering|0.09", "Niagara Falls|0.08", "Whitby|0.13", "Burlington|0.19", "Vaughan|0.32",
            "Markham|0.34", "Richmond Hill|0.20", "Oakville|0.21", "Mississauga|0.72", "Brampton|0.66"
    };
}
