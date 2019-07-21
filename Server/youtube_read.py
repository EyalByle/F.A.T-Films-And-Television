from urllib import request, parse
from urllib.error import URLError
from bs4 import BeautifulSoup
from string import printable
SEARCH = "https://www.youtube.com/results?search_query={}+trailer"
YOUTUBE_PATH = "https://www.youtube.com"
VIDEO_ATTRS = {"name": "div", "attrs": {"class": "yt-lockup-video"}}
HEADERS = {"User-Agent": "Mozilla/5.0"}
LOW_LENGTH_LIMIT = 60
HIGH_LENGTH_LIMIT = 630


def prettier(soup):
    pretty = soup.prettify()
    new = []
    for line in pretty.splitlines():
        count = 0
        for char in line:
            if char != " ":
                break
            count += 1
        new.append("\t" * count + line[count:])
    return "\n".join(new)


def length_to_int(length):
    seconds = 0
    mul = 1
    for unit in length.split(":")[::-1]:
        seconds += int(unit) * mul
        mul *= 60
    return seconds


def get_trailer_link(title, title_type, title_year=None):
    """
    :param title: Name of the title (film or show)
    :param title_type: Type of the title (TV or MOVIE)
    :param title_year: Release year
    :return: the url of the youtube trailer link
    """
    # prevents specials chars from crashing everything
    title = parse.quote(title)
    query = title.split(" ")
    query.append(title_type)

    # If an year was given, adds it to the search
    if title_year:
        query.append(title_year)

    link = SEARCH.format("+".join(query))

    # Gets the youtube search result page
    req = request.Request(link, headers=HEADERS)
    while True:
        try:  # It tries its best, OK?
            with request.urlopen(req, timeout=5) as web:
                soup = BeautifulSoup(web, 'html.parser')
        except URLError:
            continue
        break

    # Finds all results from youtube
    search_results = soup.find_all(**VIDEO_ATTRS)
    trailer_link = "None found"
    for result in search_results:
        video, info = result.contents[0].contents
        length = length_to_int(video.find("span", attrs={"class": "video-time"}).text)
        if LOW_LENGTH_LIMIT < length < HIGH_LENGTH_LIMIT:
            if "trailer" in info.find("span").text.lower():
                trailer_link = YOUTUBE_PATH + info.find("a")["href"]
                break

    # Prevents the program from being killed mercilessly
    url = parse.quote(trailer_link, safe=printable)
    return url
