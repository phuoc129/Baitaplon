import requests
from bs4 import BeautifulSoup
import pandas as pd
import schedule
import time

# Ham lay du lieu tu trang chi tiet cong viec
def get_job_data(url):
    headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"}
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        soup = BeautifulSoup(response.text, 'html.parser')

        # Tieu de
        title_tag = soup.find("h1", class_="title font-roboto text-primary")
        if title_tag:
            title = title_tag.find("span", class_="text").text.strip()
        else:
            title = "Khong co tieu de"

        # Cac the <p> chua thong tin khac
        paragraphs = soup.find_all("p")
        company = salary = location = description = "Khong ro"

        for p in paragraphs:
            text = p.get_text(strip=True)
            if "Cong ty" in text and company == "Khong ro":
                company = text
            elif "Luong" in text and salary == "Khong ro":
                salary = text
            if "Dia chi lam viec" in text:
                location = text.split("Dia chi lam viec:")[-1].strip()
            elif "Mo ta" in text or "cong viec" in text.lower():
                description = text  

        return [{
            "Tieu de": title,
            "Mo ta": description,
            "Cong ty": company,
            "Luong": salary,
            "Dia chi lam viec": location
        }]

    except requests.RequestException as e:
        print(f"Loi khi lay du lieu tu {url}: {e}")
        return []

# Ham lay cong viec tu bang danh sach
def get_jobs_from_custom_table(url):
    headers = {"User-Agent": "Mozilla/5.0"}
    job_list = []

    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        soup = BeautifulSoup(response.text, "html.parser")

        rows = soup.select("tr")
        for row in rows:
            title_td = row.select_one("td.block-item.w55")
            others_td = row.select("td.text-center.w15")

            if title_td and len(others_td) >= 2:
                job_info = title_td.get_text(separator="\n", strip=True).split("\n")
                title = job_info[0] if len(job_info) > 0 else "Khong ro"
                company = job_info[1] if len(job_info) > 1 else "Khong ro"
                location = others_td[0].get_text(strip=True)
                deadline = others_td[1].get_text(strip=True)

                job_list.append({
                    "Vi tri tuyen dung": title,
                    "Ten cong ty": company,
                    "Dia diem": location,
                    "Han nop": deadline,
                    "URL": url
                })

    except Exception as e:
        print(f"Loi khi xu ly {url}: {e}")
    return job_list

# Ham chinh ket hop ca hai loai du lieu
def main():
    all_jobs = []

    # Thu thap du lieu tu trang chi tiet cong viec
    jobs_detail = get_job_data("https://www.danang43.vn/viec-lam/nhan-vien-thiet-ke-designer-3-p69103.html")
    all_jobs.extend(jobs_detail)

    # Thu thap du lieu tu danh sach cong viec
    urls = [
        "https://www.danang43.vn/nganh-nghe/cong-nghe-thong-tin",
        "https://www.danang43.vn/nganh-nghe/cong-nghe-thong-tin/page/2"
    ]
    for url in urls:
        print(f"Dang lay du lieu tu: {url}")
        jobs_list = get_jobs_from_custom_table(url)
        all_jobs.extend(jobs_list)

    # Luu vao CSV
    if all_jobs:
        df = pd.DataFrame(all_jobs)
        df.to_csv("danang43.csv", index=False, encoding='utf-8-sig')
        print(f"Da luu {len(all_jobs)} cong viec vao 'danang43.csv'")
    else:
        print(" Khong lay duoc cong viec nao.")

# ----------- Tu dong chay vao 6h sang moi ngay ------------
schedule.every().day.at("06:00").do(main)

if __name__ == "__main__":
    print("Dang cho den 6h sang hang ngay de thu thap du lieu")
    while True:
        schedule.run_pending()
        time.sleep(60)
