import subprocess
import json
import time
import re
from bs4 import BeautifulSoup

BASE_URL = "https://www.gachon.ac.kr"
LIST_URL = "https://www.gachon.ac.kr/bbs/kor/1565/artclList.do"

def fetch_url(url, params=None):
    full_url = url
    if params:
        query = "&".join([f"{k}={v}" for k, v in params.items()])
        full_url += "?" + query
    
    try:
        # Use curl.exe -k (insecure) to bypass SSL issues
        result = subprocess.run(['curl.exe', '-s', '-k', full_url], capture_output=True, text=True, encoding='utf-8')
        if result.returncode == 0:
            return result.stdout
        else:
            print(f"Curl error: {result.stderr}")
            return None
    except Exception as e:
        print(f"Fetch error: {e}")
        return None

def get_club_list():
    clubs = []
    for page in range(1, 5):
        print(f"Fetching page {page}...")
        params = {
            'page': page,
            'layout': 'Jm/qnZgwn7jcPbb2FW+KTw==' # Extracted from HTML
        }
        html = fetch_url(LIST_URL, params)
        if not html:
            continue
            
        soup = BeautifulSoup(html, 'html.parser')
        
        items = soup.select('ul.list > li')
        for item in items:
            link_tag = item.select_one('a')
            if not link_tag:
                continue
            
            href = link_tag.get('href')
            detail_url = BASE_URL + href
            
            title_div = item.select_one('.title')
            division_text = ""
            name_text = ""
            
            if title_div:
                division_match = re.search(r'\[(.*?)\]', title_div.get_text())
                if division_match:
                    division_text = division_match.group(1).strip()
                
                name_tag = title_div.select_one('strong')
                if name_tag:
                    name_text = name_tag.get_text().strip()
                    # Remove 분과 prefix if present
                    name_text = re.sub(r'^<.*?분과_', '', name_text).strip('<>')
            
            thumb_div = item.select_one('.thumb')
            image_url = ""
            if thumb_div and 'style' in thumb_div.attrs:
                style = thumb_div['style']
                img_match = re.search(r'url\((.*?)\)', style)
                if img_match:
                    image_url = img_match.group(1).split(')')[0].strip("'\"")
                    if not image_url.startswith('http'):
                        image_url = BASE_URL + image_url

            clubs.append({
                'campus': 'Global',
                'division': division_text,
                'name': name_text,
                'detail_url': detail_url,
                'image_url': image_url,
                'short_description': name_text
            })
    return clubs

def get_club_details(club):
    print(f"Fetching details for {club['name']}...")
    html = fetch_url(club['detail_url'])
    if not html:
        return club
        
    try:
        soup = BeautifulSoup(html, 'html.parser')
        
        content_div = soup.select_one('._artclView')
        if not content_div:
            return club
        
        long_description = content_div.get_text(separator='\n', strip=True)
        club['long_description'] = long_description
        
        lines = [line.strip() for line in long_description.split('\n') if line.strip()]
        if len(lines) > 0:
            club['short_description'] = lines[0][:100]

        location_match = re.search(r'(위치|동아리방)[:\s]+([^\n]+)', long_description)
        if location_match:
            club['location'] = location_match.group(2).strip()
        else:
            club['location'] = ""

        contact_match = re.search(r'(연락처|문의|회장|SNS|인스타그램|insta|@)[:\s]+([^\n]+)', long_description, re.IGNORECASE)
        if contact_match:
            club['contact'] = contact_match.group(2).strip()
        else:
            club['contact'] = ""

        if not club['image_url'] or 'thumb_temp' in club['image_url']:
            img_tags = content_div.select('img')
            for img in img_tags:
                src = img.get('src')
                if src and not src.endswith('.gif'):
                    if not src.startswith('http'):
                        src = BASE_URL + src
                    club['image_url'] = src
                    break

    except Exception as e:
        print(f"Error processing {club['name']}: {e}")
    
    return club

def main():
    clubs = get_club_list()
    print(f"Found {len(clubs)} clubs. Fetching details...")
    
    full_clubs = []
    for club in clubs:
        full_clubs.append(get_club_details(club))
        time.sleep(0.5) # Avoid overwhelming the server
        
    # Clean up fields not needed in final JSON
    for club in full_clubs:
        if 'detail_url' in club:
            del club['detail_url']

    output_path = "app/src/main/assets/clubs_global.json"
    import os
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(full_clubs, f, ensure_ascii=False, indent=2)
    
    print(f"Saved {len(full_clubs)} clubs to {output_path}")

if __name__ == "__main__":
    main()
