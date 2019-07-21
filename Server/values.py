from calendar import month_abbr
from string import ascii_letters, digits
from itertools import permutations
from random import randint, choice


# Options for separators of an array
ARRAY_SEPARATORS = set(["-{}{}-".format(*x) for x in
                        permutations(ascii_letters, 2)])

# Options for special separators of an array
SPECIAL_SEPARATORS = set(["```{}{}{}```".format(*x) for x in
                          permutations(ascii_letters, 3)])

PIN_LIST = digits  # Pin only contains digits
PIN_LENGTH = (5, 10)  # Between 5 and 10

# Months dictionary (First 3 letters of month: number)
MONTHS = dict((v, str(k)) for k, v in enumerate(month_abbr))

# All terms imdb_read should fetch
IMDB_TERMS = [
    "parents_guide", "length", "genres", "date", "director",
    "producer", "writer", "cast", "plot", "poster",
    "languages", "seasons", "episodes"
]

# One thing can have many names
TRANSLATE = {
    "directed by": "director",
    "produced by": "producer",
    "writing credits": "writer",
    "written by": "writer",
    "length": "duration",
    "original language(s)": "languages"
}

# BeautifulSoup.find(...)
HTML_NAMES = {
    "episodesImdb": {
        "name": "div",
        "attrs": {"class": "button_panel navigation_panel"}
    },
    "seasonsImdb": {
        "name": "div",
        "attrs": {"class": "seasons-and-year-nav"}
    },
    "plotImdb": {
        "name": "div",
        "attrs": {"class": "summary_text"}
    },
    "detailsImdb": {
        "name": "div",
        "attrs": {"class": "article", "id": "titleDetails"}
    },
    "jsonImdb": {
        "name": "script",
        "attrs": {"type": "application/ld+json"}
    },
    "headersImdb": {
        "name": "h4",
        "attrs": {"class": "dataHeaderWithBorder"}
    },
    "searchImdb": {
        "name": "h3",
        "attrs": {'class': 'lister-item-header'}
    },
    "yearImdb": {
        "name": "span",
        "attrs": {"class": "lister-item-year text-muted unbold"}
    }
}

# Things that are usually in the json
IMDB_VALUES = {
    "parents_guide": "contentRating",
    "genres": "genre",
    "date": "datePublished"
}

# Co-responding counter of the average
TITLE_NUMBERS = {
    "rating": "raters_count"
}


def is_float(string):
    """
    :param string: A number
    :return: If the number is a float
    """
    try:
        return float(string) or True
    except ValueError:
        return False


def find_sep(string, titles=False, maximum=0):
    """
    Finds a suitable separator for a given array
    :param string: Just a string
    :param titles: Whether or not should the function use special separators
    :param maximum: Maximum separators required
    :return:
    """
    if titles:
        separators = SPECIAL_SEPARATORS
    else:
        separators = ARRAY_SEPARATORS
    answer = []
    for sep in separators:
        if sep not in string:
            answer.append(sep)
            if len(answer) == maximum:
                break
    return answer


def split_string(string, ret_sep=False):
    """
    Basically, it's the opposite of join_arr
    :param string: A single string
    :param ret_sep: Whether it should return the used separator or not
    :return: an array of strings and potentially the separator
    """
    try:
        if not len(string):
            raise IndexError
        sep = string[string.find("(") + 1:string.find(")")]
        string = string[string.find(")") + 1:]
    except IndexError:
        return string, 0
    if ret_sep:
        return string.split(sep), sep
    return string.split(sep)


def join_arr(*strings):
    """
    Basically, it's the opposite of split_string
    :param strings: an array of strings
    :return: A single joined string
    """
    sep = find_sep(str(strings), maximum=1)[0]
    return "({}){}".format(sep, sep.join(strings))


def gen_pin_code():
    length = randint(*PIN_LENGTH)
    pin = ""
    for _ in range(length):
        pin += choice(PIN_LIST)
    print("Pin code:", pin)
    return pin


def is_english(string):
    try:
        string.encode("utf-8").decode("ascii")
    except UnicodeDecodeError:
        return False
    return True
