# -*- coding: utf-8 -*-
import os
from mitmproxy import http
import rmp2json

root_path = 'F:/rhythm/song/'


def get_diff(url):
    if '_ez.rmp' in url:
        return 'ez'
    if '_nm.rmp' in url:
        return 'nm'
    if '_hd.rmp' in url:
        return 'hd'


def get_key(url):
    index = url.find("_")  # 找到第一个"_"的索引
    number_index = index + 1  # 数字的索引是"_"的索引加1
    return url[number_index]  # 提取数字


def get_diysong_name(cover_path):
    song_name = ""
    if os.path.exists(root_path + cover_path):
        for f in os.listdir(root_path + cover_path + '/'):
            if f.endswith('.mp3') and f.find('_prev.mp3') == -1:
                song_name = f
                return song_name[:-4]
    return song_name


# how-to-start: mitmweb -s C:/节奏大师/自制谱工具/proxy.py --no-showhost
class SimpleProxy:
    def response(self, flow: http.HTTPFlow) -> None:
        # if flow.request.host not in self.LIST_DOMAINS:
        #     return

        if '/SongRes/song/' in flow.request.url:
            url = flow.request.url
            start_index = url.find('/SongRes/song/') + len('/SongRes/song/')
            end_index = url.find('/', start_index)
            song_name = "error"
            diff = get_diff(url)
            if start_index != -1 and end_index != -1:
                song_name = url[start_index:end_index]
            src_path = song_name
            cover_path = song_name

            if not cover_path:
                print('Found available path: ' + src_path)
                return

            request_url = flow.request.url
            if '?' in request_url:
                request_url = request_url.split('?')[0]

            real_name = get_diysong_name(cover_path)
            # MP3
            if request_url.endswith('.mp3'):

                if request_url.endswith('_prev.mp3'):
                    mp3_prev_path = cover_path + '/' + real_name + '_prev.mp3'
                    file_path = root_path + mp3_prev_path
                    if not os.path.exists(file_path):
                        return
                    f = open(file_path, 'rb')
                    flow.response.content = f.read()
                    f.close()
                    print('MP3(prev) Replace: %s -> %s' % (src_path, real_name))
                    return
                else:
                    mp3_path = cover_path + '/' + real_name + '.mp3'
                    file_path = root_path + mp3_path
                    if not os.path.exists(file_path):
                        return
                    f = open(file_path, 'rb')
                    flow.response.content = f.read()
                    f.close()
                    print('MP3 Replace: %s -> %s' % (src_path, real_name))
                    return

            if request_url.endswith('.rmp'):
                key = get_key(request_url)
                imd_path = f"{cover_path}/{real_name}_{key}k_{diff}"
                key_path = f"{cover_path}_{key}k_{diff}"
                dst_rmp_path = root_path + imd_path + '.rmp'
                dst_json_path = root_path + imd_path + '.imd.json'
                dst_imd_path = root_path + imd_path + '.imd'
                if os.path.exists(dst_rmp_path):
                    f = open(dst_rmp_path, 'r')
                    flow.response.text = f.read()
                    f.close()
                    print('RMP Replace (rmp): %s -> %s' % (src_path, real_name))
                    return
                elif os.path.exists(dst_json_path):
                    print(key_path)
                    # json_to_rmp
                    rmp2json.encrypt(dst_json_path, key_path, root_path + imd_path)
                    f = open(dst_rmp_path, 'r')
                    flow.response.text = f.read()
                    f.close()
                    print('RMP Replace (json): %s -> %s' % (src_path, real_name))
                    return
                elif os.path.exists(dst_imd_path):
                    # imd_to_json
                    rmp2json.imd_to_json(dst_imd_path, dst_json_path, key, "1.2.1")
                    # json_to_rmp
                    rmp2json.encrypt(dst_json_path, key_path, root_path + imd_path)
                    f = open(dst_rmp_path, 'r')
                    flow.response.text = f.read()
                    f.close()
                    print('RMP Replace (imd): %s -> %s' % (src_path, real_name))
                    return
            if request_url.endswith('_ipad.jpg'):
                jpg_path = f"{cover_path}/{real_name}_ipad"
                dst_jpg_path = root_path + jpg_path + '.jpg'
                if os.path.exists(dst_jpg_path):
                    f = open(dst_jpg_path, 'rb')
                    flow.response.content = f.read()
                    f.close()
                    print('jpg Replace: %s -> %s' % (src_path, real_name))
                    return

            if request_url.endswith('_thumb.jpg'):
                jpg_path = f"{cover_path}/{real_name}_thumb"
                dst_jpg_path = root_path + jpg_path + '.jpg'
                if os.path.exists(dst_jpg_path):
                    f = open(dst_jpg_path, 'rb')
                    flow.response.content = f.read()
                    f.close()
                    print('jpg Replace: %s -> %s' % (src_path, real_name))
                    return
            if request_url.endswith('_ipad.png'):
                jpg_path = f"{cover_path}/{real_name}_ipad"
                dst_jpg_path = root_path + jpg_path + '.png'
                if os.path.exists(dst_jpg_path):
                    f = open(dst_jpg_path, 'rb')
                    flow.response.content = f.read()
                    f.close()
                    print('png Replace: %s -> %s' % (src_path, real_name))
                    return

            if request_url.endswith('_thumb.png'):
                jpg_path = f"{cover_path}/{real_name}_thumb"
                dst_jpg_path = root_path + jpg_path + '.png'
                if os.path.exists(dst_jpg_path):
                    f = open(dst_jpg_path, 'rb')
                    flow.response.content = f.read()
                    f.close()
                    print('png Replace: %s -> %s' % (src_path, real_name))
                    return
        print('Skip URL: ' + flow.request.url)


addons = [
    SimpleProxy()
]
