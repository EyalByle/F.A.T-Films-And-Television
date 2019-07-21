"""
Reads stuff from IMDb
"""
from urllib import request, parse
from urllib.error import URLError
from bs4 import BeautifulSoup
from bs4.element import Tag as BS4Tag
from re import search, findall
from pprint import pprint

from values import *
IMDB_SEARCH = "https://www.imdb.com/search/title?title={}&" \
              "title_type={}&release_date={},"
DEFAULT_TIME = "0000-00-00"
IMDB = "https://www.imdb.com/"
OPTIONS = {
    "Show": "tv_series,tv_miniseries",
    "Movie": "feature,tv_movie,short"
}
TIMES_TRANSLATE = {"h": 60, "m": 1}


def get_imdb_title(name, title_type, date=DEFAULT_TIME):
    """
    :param name: title's name
    :param title_type: title's type
    :param date: title's release date
    :return: fixed name, fixed date, imdb link
    """
    name = name.lower()
    name2 = parse.quote(name)  # Prevent special chars from crashing it all
    link = IMDB_SEARCH.format(name2, OPTIONS[title_type], date)  # Formats it
    print("Search link: ", link)
    # Gets the imdb search result page
    while True:
        try:  # It tries it best, OK?
            with request.urlopen(link, timeout=5) as web:
                soup = BeautifulSoup(web, 'html.parser')
        except URLError:
            continue
        break

    # Gets all results from the search
    search_results = soup.find_all(**HTML_NAMES["searchImdb"])

    # # If there are no search results
    if not len(search_results):
        print("IMDB search result returned nothing -> Search failed.")
        return 0, 0, "Failure"

    # Loops through the items in the search result
    # Stops when finds actual result that fits the date and name
    search_year = date.split("-")[0]
    for result in search_results:

        # Searches for the release year of each result
        year = result.find(**HTML_NAMES["yearImdb"]).text
        options = search(r"\((\d+)\)", year)

        if options:
            # Found an year
            year = options.group(1)
        else:
            # Searches the year with another regex
            options = search(r"\(([^)]+)\)", year)
            if not options:
                continue
            year = options.group(1)
            year = year[:4]

        # Checks if date is correct
        if date != DEFAULT_TIME and year != search_year:

            # Bad date
            print(date, DEFAULT_TIME, year, search_year)
            continue

        # Checks if name is correct
        if name in result.a.text.lower():

            # Name is correct ==> Found a title
            name = result.a.text
            date = year
            href = result.a["href"]
            break
    else:
        # It didn't break -> didn't find a link
        href = search_results[0].a["href"]

    # href is weird, fixing it
    url = []
    for part in href.split("/"):
        if part and not part.startswith("?"):
            url.append(part)

    print(url)
    return name, date, IMDB + "/".join(url)


def get_time(string):
    """
    :param string: Makes imdb time string presentable to user
    :return: presentable time string
    """
    minutes = 0
    if string.startswith("PT"):
        string = string[2:].lower()
    for value, unit in findall(r"([\d].?)(?=([hm]))", string):
        minutes += int(value) * TIMES_TRANSLATE[unit]
    return str(minutes) + "min"


def fix_date(date):
    """
    :param date: Formats imdb date as YYYY-MM-DD
    :return: Formatted date
    """
    # 3 April 2003 (Country) --> 2003-04-03
    st = []
    for word in date.split()[::-1]:
        if word.isdigit():
            st.append(word.zfill(2))
        elif word[:3] in MONTHS:
            st.append(MONTHS[word[:3]].zfill(2))
    st = "-".join(st)
    return st


def get_cast(soup, title_type):
    """
    :param soup: soup of title's cast taken directly from imdb
    :param title_type: type of the title
    :return: string that represents all of the cast to the title
    """
    personals = []
    print("Entered cast")

    # Loops through the soup
    for person in soup:

        # I don't need to check non-Tags
        if not isinstance(person, BS4Tag):
            continue
        character = []

        # Removes empty lines and strips the text
        for line in person.text.splitlines():
            line = line.strip()
            if line:
                character.append(line)

        # If line was only possible to strip characters (Empty line)
        if not character:
            continue

        # Removes episodes counter for shows
        if title_type == "Show":
            new = character[:-1]
            if len(new):
                character = new

        # ["name", "...", "role"] -> ["name", "role"]
        character = ("".join(character)).split("...")

        # If character has both actor and role, adds it
        if len(character) > 1:
            if not character[-1]:
                character = character[:2]
                character[1] = "/".join(character[1].split("/")[:-1])
            personals.append(character)

        # Deletes character because code doesn't work otherwise (Never mind)
        # del character

    # Finds usable separators (Not in the original string -> usable)
    separators = find_sep(str(personals), maximum=2)
    complete_cast = ["({0})({1})".format(*separators)]

    # Joins the name and the role of each character and adds it to the list
    for character in personals:
        complete_cast.append(separators[1].join(character))
    # Joins the cast list and returns it
    return separators[0].join(complete_cast)


def show_sorting(crew_members):
    """
    :param crew_members: list of crew members for a show
                         Usually ("name", "...", "role (x episodes, years)")
    :return: a sorted version of the given list by the amount of episodes
    """
    new_list = []
    for person in crew_members:
        # ("person's name", '...', '([int] episodes, year1-year2)')
        # episodes will be            ^^^
        episodes = person[-1].split("(")[-1].split()[0]

        # Episodes could also be a string ("unknown" for example)
        if not episodes.isdigit():
            episodes = 0
        episodes = int(episodes)

        # Adds to the list the amount of episodes, the first letter of the
        # first name of the person and the person
        new_list.append((episodes, person))

    # Sorts the list "new_list" by episodes count (more episodes->lower index)
    # and by alphabetical order of their names
    sorted_list = [l[-1] for l in sorted(new_list,
                                         key=lambda tup: (-tup[0], tup[1]))]
    return sorted_list


def get_crew_by_key(soup, key, title_type):
    """
    :param soup: soup of title's cast taken directly from imdb
    :param key: Job of the crew members the function should return
    :param title_type: type of the title
    :return: string that represents all of the crew to the title
             of the specific key
    """
    employees = []
    soup = soup.tbody
    print("Key:", key)

    # Loops through the soup
    for person in soup:

        # I don't need to check non-Tags
        if not isinstance(person, BS4Tag):
            continue

        # Removes empty lines and strips the text
        character = []
        for cell in person.text.split("\n"):
            cell = cell.strip()
            if cell:
                character.append(cell)

        # If it is not empty after the removes, adds it
        if character:
            employees.append(character)

    # Sorts the employees of a show by episode count and name
    if title_type == "Show":
        employees = show_sorting(employees)

    # Creates a new array to put the actual values in
    crew_members = []
    index = 0

    # Loops through the sorted list of crew members
    while index < len(employees):

        # ["name", "...", "role"] -> ["name", "role"]
        character = ("".join(employees[index])).split("...")

        # If character is not empty
        if len(character) >= 1:
            crew_members.append(character[0])
        index += 1

    # If crew_members is empty, gives it a default value
    if not crew_members:
        crew_members = ["None found"]

    # finds separator
    separators = find_sep(crew_members, maximum=1)

    # Joins the crew list and returns it
    return "({}){}".format(separators[0], separators[0].join(crew_members))


def get_all_crew(link, requests, title_type):
    """
    :param link: imdb link of the title
    :param requests: requests...
    :param title_type: type of the title
    :return: dictionary of crew members for each request
    """
    print("Requests:\t\t", requests)

    # Gets the soup of the full credits
    link += "/fullcredits"
    print(link)
    while True:
        try:
            with request.urlopen(link, timeout=5) as web:
                full_credits = BeautifulSoup(web, 'html.parser')
        except URLError:
            continue
        break
    print("Got crew page")

    # Finds all headers in the full credits page of the title
    # Headers are sections, for instance: cast, directors, writers and so on
    headers = full_credits.findAll(**HTML_NAMES["headersImdb"])
    dictionary = dict()

    # Loops through the headers
    for header in headers:
        key = header.text

        # Takes only the necessary part of the header (which is the key)
        if "(" in key:
            key = key[:key.index("(")]
        key = key.strip().lower()

        # If the title is a show, then there is "Series" before every header
        if title_type == "Show":
            key = " ".join(key.split()[1:])
        else:
            key = key.split("\n")[0]

        # If key is one of the things I need to find
        key = TRANSLATE.get(key, key)
        if key in requests:
            requests.remove(key)
            print("IN requests:", key)

            # Finds the closest table (which contains the crew for the header)
            table = header.find_next("table")

            # Cast has a different protocol
            if key == "cast":
                values = get_cast(table, title_type)
            else:
                values = get_crew_by_key(table, key, title_type)

            # The letter s will be added to the end of the key unless
            # the key is "cast" because cast is the plural of cast
            key += "s" * (key != "cast")
            dictionary[key] = values

    # Adds default values to every key it didn't find
    for req in requests:
        req += "s" * (req != "cast")
        dictionary[req] = "None found"
    return dictionary


def get_other_data(soup, requests, title_type):
    """
    :param soup: raw imdb page
    :param requests: requests
    :param title_type:
    :return: requested things
    """
    dictionary = {}

    if title_type == "Show":
        # Finds episodes in imdb page
        if "episodes" in requests:
            episodes_box = soup.find(**HTML_NAMES["episodesImdb"])
            episodes = episodes_box.span.text.split()[0]
            if "[" in episodes:
                episodes = episodes[:episodes.find("[")]
            dictionary["episodes"] = episodes
            requests.remove("episodes")

        # Finds seasons in imdb page
        if "seasons" in requests:
            seasons_box = soup.find(**HTML_NAMES["seasonsImdb"])
            if seasons_box is None:
                dictionary["seasons"] = "N/A"
            else:
                seasons_box = seasons_box.contents
                i = 0
                while i < len(seasons_box):
                    if isinstance(seasons_box[i], BS4Tag):
                        if seasons_box[i].text:
                            i += 1
                            continue
                    seasons_box.pop(i)

                # I only need the third one
                seasons = list(seasons_box[2].stripped_strings)[0]
                if "[" in seasons:
                    seasons = seasons[:seasons.find("[")]
                dictionary["seasons"] = seasons
            requests.remove("seasons")
    else:
        if "episodes" in requests:
            requests.remove("episodes")
        if "seasons" in requests:
            requests.remove("seasons")

    # If the program didn't get the plot from the json
    if "plot" in requests:
        plot = soup.find(**HTML_NAMES["plotImdb"])
        if plot:
            dictionary["plot"] = plot.text.strip().split("\n")[0]
            requests.remove("plot")

    # If the program didn't get the image from the json
    if "poster" in requests:
        poster = soup.find("div", attrs={"class": "poster"})
        if poster:
            dictionary["poster"] = poster.img["src"]
            requests.remove("poster")

    # If the program didn't get the languages from the json
    if "languages" in requests:
        details = soup.find(**HTML_NAMES["detailsImdb"])
        print("Searching for Language in details of title...")
        for thing in details.find_all("div", attrs={"class": "txt-block"}):
            thing = thing.text.strip().split("\n")
            if thing[0].startswith("Language") and len(thing) > 1:
                print(thing)
                thing = list(filter(lambda x: len(x) > 1, thing))[1:]
                requests.remove("languages")
                dictionary["languages"] = ", ".join(thing)
                print("Found language: ", thing)
                break

    return dictionary, requests


def get_necessities(link, requests, title_type):
    """
    working for movies, not for shows
    :param link: url of the imdb page
    :param title_type: type of the title
    :param requests: the things that are needed from the read
    :return: data from the imdb page
    """
    print("Requests:", requests)
    while True:
        try:
            with request.urlopen(link, timeout=5) as web:
                soup = BeautifulSoup(web, 'html.parser')
        except URLError:
            continue
        break
    items = eval(soup.find(**HTML_NAMES["jsonImdb"]).contents[0])
    dictionary = {}

    print("\nStarted reading from json of title")
    for k, v in IMDB_VALUES.items():
        if k in requests:
            v = items.get(v, "Error")
            print("Adding:\t", k, "\t Value:\t", v)
            dictionary[k] = v
            requests.remove(k)
    print("Finished taking data from json")
    print("Updated requests:", requests)

    # If the program didn't get the length from the json
    if "length" in requests:
        if TRANSLATE["length"] in items:
            print(1)
            dictionary["length"] = get_time(items[TRANSLATE["length"]])
        else:
            length = soup.find("div", "subtext").text.split("|")
            print(length)
            dictionary["length"] = get_time(length[0].strip())

    # If the program did find the genres from the json
    # Fixing the formatting
    if "genres" in dictionary:
        if isinstance(dictionary["genres"], list):
            dictionary["genres"] = ", ".join(dictionary["genres"][:3])

    # Updating requests for any remove I forgot
    print("Updated requests: ", requests)
    i = 0
    while i < len(requests):
        req = requests[i]
        if req in dictionary:
            requests.pop(i)
        else:
            i += 1
    print("Requests after popping: ", requests)

    # All extra things that the auto-find didn't find
    if requests:
        result, requests = get_other_data(soup, requests, title_type)
        dictionary.update(result)

    # Getting all crew members
    dictionary.update(get_all_crew(link, requests, title_type))

    return dictionary
