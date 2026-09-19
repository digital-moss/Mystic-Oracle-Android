import os
import urllib.request
import json
import zipfile

api_url = "https://api.github.com/repos/searge/tarot/contents/assets/img/big"
req = urllib.request.Request(api_url, headers={'User-Agent': 'Mozilla/5.0'})
try:
    res = urllib.request.urlopen(req)
    items = json.loads(res.read().decode())
except Exception as e:
    print(f"Failed to fetch github contents: {e}")
    items = []

output_dir = "app/src/main/assets/rider_waite_tarot"
os.makedirs(output_dir, exist_ok=True)

success_count = 0
for item in items:
    if item.get('type') == 'file' and item.get('download_url'):
        fname = item['name']
        download_url = item['download_url']
        dest = os.path.join(output_dir, fname)
        try:
            urllib.request.urlretrieve(download_url, dest)
            success_count += 1
            print(f"[{success_count}] Downloaded {fname}")
        except Exception as e:
            print(f"Failed to download {fname}: {e}")

# Create zip file in assets
zip_path = "app/src/main/assets/tarot_big_images.zip"
print(f"Creating zip archive {zip_path}...")
with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
    for root, dirs, files in os.walk(output_dir):
        for file in files:
            file_path = os.path.join(root, file)
            arcname = os.path.relpath(file_path, output_dir)
            zipf.write(file_path, arcname)

print(f"Done! Downloaded {success_count} images and created zip at {zip_path}.")
