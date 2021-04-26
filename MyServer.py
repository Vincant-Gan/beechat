# 开发时间：2021/4/15  21:42

from socket import *
import os
import threading
import time


# 将
def send_file_2_client(file_name, client_socket):
    # file_content = None
    try:
        f = open(file_name, 'rb')
        file_content = f.read()

        server_data_size = 'server_data_size:'+str(len(file_content))
        client_socket.sendall(server_data_size.encode('utf-8'))
        client_ack = client_socket.recv(50).decode('utf-8')
        if client_ack == 'ok':
            if file_content:
                client_socket.send(file_content)

        f.close()
    except Exception as ret:
        print('没有要下载的文件(%s)' % file_name)
        client_socket.send('No such file')


# def receive_file_from_client(client_socket, source_uid, target_uid):


# 密码，hush解码函数
def hush_decode(password):
    pass


# 申请新账户时，遍历所有uid，若无重复项则在ID_Password.txt文件末尾添加新id项，建立用户文件夹，返回True
def check_update_uid(uid, password):
    with open('ID_Passwords.txt', 'a+', encoding='utf-8') as file:
        file.seek(0)
        uid_lst = file.readlines()
        for item in uid_lst:
            uid_item = item.split()
            if uid_item[0] == uid:
                return False
        else:
            file.seek(0, 2)
            file.write(uid+' '+password+' \n')

            folder = os.getcwd() + '\\'+uid
            if not os.path.exists(folder):
                os.makedirs(folder)

            return True


# 登录时检查密码，匹配成功返回True
def check_password(uid, password):
    with open('ID_Passwords.txt', 'a+', encoding='utf-8') as file:
        file.seek(0)
        uid_lst = file.readlines()
        for item in uid_lst:
            uid_item = item.split()
            if uid_item[0] == uid:
                if uid_item[1] == password:
                    return 1
                else:
                    return 2
        return 0
        ''' 
        item = uid_lst[int(uid)-1].split()
        real_pass_word = item[1]
        if real_pass_word == password:
            return True
        else:
            return False'''


# def deal_with_msg_type():
def server_thread(client_socket, client_addr):
    global online_dic, id_to_csocket, online_dic_lock, id_password_lock, history_lock
    while True:
        login_data = client_socket.recv(1024).decode('utf-8')
        print('login_data:', login_data)
        if login_data == 'offline':
            break

        login_lst = login_data.split()
        print(login_lst)
        # login_lst[2] = hush_decode(login_lst[2])

        if login_lst[0] == 'signup':
            # id_password文件锁
            id_password_lock.acquire()
            uid_check = check_update_uid(login_lst[1], login_lst[2])
            id_password_lock.release()
            if uid_check:
                client_socket.send('signup successfully'.encode('utf-8'))
            else:
                client_socket.send('uid already exists'.encode('utf-8'))

        elif login_lst[0] == 'signin':
            # id_password文件锁
            id_password_lock.acquire()
            pass_check = check_password(login_lst[1], login_lst[2])
            id_password_lock.release()
            if pass_check == 1:
                client_socket.send('signin successfully'.encode('utf-8'))
                # 更新在线记录字典，在线字典锁
                online_dic_lock.acquire()
                online_dic[login_lst[1]] = True
                id_to_csocket[login_lst[1]] = client_socket
                online_dic_lock.release()
                while True:
                    recv_command = client_socket.recv(1024).decode('utf-8')
                    # 现假设下线客户端发送空信息
                    if recv_command == 'offline':
                        # 在线字典锁
                        online_dic_lock.acquire()
                        del online_dic[login_lst[1]]
                        del id_to_csocket[login_lst[1]]
                        online_dic_lock.release()
                        break

                    recv_command_lst = recv_command.split(sep='\n', maxsplit=1)
                    command_lst = recv_command_lst[0].split()

                    # 根据id大小，唯一确定聊天记录文件夹名
                    if command_lst[0] <= command_lst[2]:
                        folder = os.getcwd() + '\\' + command_lst[0] + ' ' + command_lst[2]
                    else:
                        folder = os.getcwd() + '\\' + command_lst[2] + ' ' + command_lst[0]

                    # 根据聊天软件设计，分为to功能和history功能
                    if command_lst[1] == 'to':
                        # 若尚不存在聊天记录文件夹，则创建
                        # 历史文件锁
                        history_lock.acquire()
                        if not os.path.exists(folder):
                            os.makedirs(folder)
                        # 若尚不存在聊天记录文件，则创建
                        with open(folder+'\\'+'history.txt', 'a+', encoding='utf-8') as file:
                            file.write(recv_command)
                        history_lock.release()

                        # 如果是文字内容，则直接由目标id的client_socket帮忙转发
                        if command_lst[4] == 'text':
                            id_to_csocket[command_lst[2]].sendall(recv_command.encode('utf-8'))
                        else:
                            # 否则，根据第五项的文件大小，确定接收次数，并发送'ok'表示开始接收
                            client_size = int(command_lst[5])
                            receive_size = 0
                            res = "".encode('utf-8')
                            client_socket.send('ok'.encode('utf-8'))
                            while receive_size < client_size:
                                server_data = client_socket.recv(1024)
                                receive_size += len(server_data)
                                # bytes变量能否用 += 符号进行运算
                                res += server_data
                            file_name = ''
                            # audio文件命名方式为 timestamp+'_audio.'
                            if command_lst[4] == 'audio':
                                file_name = folder+'\\'+command_lst[3]+'_audio.'
                            # file文件命名方式为即为用户对文件的命名，这意味着不能存储名字相同的文件
                            elif command_lst[4] == 'file':
                                file_name = folder+'\\' + recv_command_lst[1]
                            # 历史文件锁
                            history_lock.acquire()
                            with open(file_name, 'wb') as file:
                                file.write(res)
                            history_lock.release()

                            # 本地存储后开始向目标id转发
                            # 判断对方是否在线，若不在线则返回'Target user offline'
                            if online_dic.get(command_lst[2]):
                                id_to_csocket[command_lst[2]].sendall(recv_command.encode('utf-8'))
                                file_content = res
                                server_data_size = 'server_data_size:'+str(len(file_content))
                                client_socket.sendall(server_data_size.encode('utf-8'))
                                client_ack = client_socket.recv(50).decode('utf-8')
                                if client_ack == 'ok':
                                    if file_content:
                                        client_socket.send(file_content)
                            else:
                                client_socket.send('Target user offline'.encode('utf-8'))
                            # send_file_2_client(file_name, client_socket)

                    elif command_lst[1] == 'history':
                        # 历史文件锁，注意：此处锁可能会占用时间较长
                        history_lock.acquire()
                        if not os.path.exists(folder):
                            client_socket.send('you two have no history yet'.encode('utf-8'))
                        elif command_lst[3] == 'all':
                            send_file_2_client(folder+'\\'+'history.txt', client_socket)
                        elif command_lst[3] == 'filename':
                            send_file_2_client(folder+'\\'+command_lst[4], client_socket)
                        history_lock.release()
                    else:
                        client_socket.send('Wrong interact command'.encode('utf-8'))
                # 若从登录模式退出，则意味着该client_socket不再服务，故break，关闭client_socket.close()
                break
            elif pass_check == 0:
                client_socket.send('uid not exits'.encode('utf-8'))
            else:
                client_socket.send('Wrong password'.encode('utf-8'))
        else:
            client_socket.send('Wrong sign command'.encode('utf-8'))
    client_socket.close()


def main():
    tcp_server_socket = socket(AF_INET, SOCK_STREAM)
    tcp_server_socket.bind(('', 7890))
    tcp_server_socket.listen(128)
    print('Start listen')
    while True:
        client_socket, client_addr = tcp_server_socket.accept()
        print('client_socket:', client_socket)
        print("client_addr:", client_addr)
        t = threading.Thread(target=server_thread, args=(client_socket, client_addr))
        t.start()
        threads.append(t)

    for thread in threads:
        thread.join()
    tcp_server_socket.close()


if __name__ == '__main__':
    # 在线用户字典，在线则为True
    online_dic = {}
    # 在线uid对应的client_socket，用于聊天调用转发消息
    id_to_csocket = {}
    csocket_to_id = {}
    online_dic_lock = threading.Lock()
    id_password_lock = threading.Lock()
    history_lock = threading.Lock()
    threads = []
    main()
'''
    uid = '789456'
    password = '654321'
    print(check_update_uid(uid, password))
    print(check_password(uid, password))'''


'''
                                elif command_lst[4] == 'file':
                                    client_size = int(command_lst[5])
                                    receive_size = 0
                                    res = "".encode('utf-8')
                                    while receive_size < client_size:
                                        server_data = client_socket.recv(1024)
                                        receive_size += len(server_data)
                                        res += server_data
                                    file_name = folder+'\\' + command_lst[6]
                                    with open(file_name, 'wb') as file:
                                        file.write(res)
                                    id_to_csocket[command_lst[2]].sendall(recv_command.encode('utf-8'))
                                    send_file_2_client(file_name, client_socket)'''

'''while True:
            login_data = client_socket.recv(1024).decode('utf-8')
            print('login_data:',login_data)
            if not login_data:
                break

            login_lst = login_data.split()
            print(login_lst)
            # login_lst[2] = hush_decode(login_lst[2])

            if login_lst[0] == 'signup':
                if check_update_uid(login_lst[1], login_lst[2]):
                    client_socket.send('signup successfully'.encode('utf-8'))
                else:
                    client_socket.send('uid already exists'.encode('utf-8'))

            elif login_lst[0] == 'signin':
                pass_check=check_password(login_lst[1], login_lst[2])
                if pass_check == 1:
                    client_socket.send('signin successfully'.encode('utf-8'))
                    # 更新在线记录字典
                    online_dic[login_lst[1]] = True
                    id_to_csocket[login_lst[1]] = client_socket
                    while True:
                        recv_command = client_socket.recv(1024).decode('utf-8')
                        # 现假设下线客户端发送空信息
                        if not recv_command:
                            del online_dic[login_lst[1]]
                            del id_to_csocket[login_lst[1]]
                            break

                        recv_command_lst = recv_command.split(sep='\n', maxsplit=1)
                        command_lst = recv_command_lst[0].split()

                        # 根据id大小，唯一确定聊天记录文件夹名
                        if command_lst[0] <= command_lst[2]:
                            folder = os.getcwd() + '\\' + command_lst[0] + ' ' + command_lst[2]
                        else:
                            folder = os.getcwd() + '\\' + command_lst[2] + ' ' + command_lst[0]

                        # 根据聊天软件设计，分为to功能和history功能
                        if command_lst[1] == 'to':
                            # 若尚不存在聊天记录文件夹，则创建
                            if not os.path.exists(folder):
                                os.makedirs(folder)
                            # 若尚不存在聊天记录文件，则创建
                            with open(folder+'\\'+'history.txt', 'a+', encoding='utf-8') as file:
                                file.write(recv_command)
                            # 如果是文字内容，则直接由目标id的client_socket帮忙转发
                            if command_lst[4] == 'text':
                                id_to_csocket[command_lst[2]].sendall(recv_command.encode('utf-8'))
                            else:
                                # 否则，根据第五项的文件大小，确定接收次数，并发送'ok'表示开始接收
                                client_size = int(command_lst[5])
                                receive_size = 0
                                res = "".encode('utf-8')
                                client_socket.send('ok'.encode('utf-8'))
                                while receive_size < client_size:
                                    server_data = client_socket.recv(1024)
                                    receive_size += len(server_data)
                                    # bytes变量能否用 += 符号进行运算
                                    res += server_data
                                file_name = ''
                                # audio文件命名方式为 timestamp+'_audio.'
                                if command_lst[4] == 'audio':
                                    file_name = folder+'\\'+command_lst[3]+'_audio.'
                                # file文件命名方式为即为用户对文件的命名，这意味着不能存储名字相同的文件
                                elif command_lst[4] == 'file':
                                    file_name = folder+'\\' + recv_command_lst[1]
                                with open(file_name, 'wb') as file:
                                    file.write(res)

                                # 本地存储后开始向目标id转发
                                # 判断对方是否在线，若不在线则返回'Target user offline'
                                if online_dic.get(command_lst[2]):
                                    id_to_csocket[command_lst[2]].sendall(recv_command.encode('utf-8'))
                                    file_content = res
                                    server_data_size = 'server_data_size:'+str(len(file_content))
                                    client_socket.sendall(server_data_size.encode('utf-8'))
                                    client_ack = client_socket.recv(50).decode('utf-8')
                                    if client_ack == 'ok':
                                        if file_content:
                                            client_socket.send(file_content)
                                else:
                                    client_socket.send('Target user offline'.encode('utf-8'))
                                # send_file_2_client(file_name, client_socket)

                        elif command_lst[1] == 'history':
                            if not os.path.exists(folder):
                                client_socket.send('you two have no history yet'.encode('utf-8'))
                            elif command_lst[3] == 'all':
                                send_file_2_client(folder+'\\'+'history.txt', client_socket)
                            elif command_lst[3] == 'filename':
                                send_file_2_client(folder+'\\'+command_lst[4], client_socket)
                    # 若从登录模式退出，则意味着该client_socket不再服务，故break，关闭client_socket.close()
                    break
                elif pass_check == 2:
                    client_socket.send('uid not exits'.encode('utf-8'))
                else:
                    client_socket.send('Wrong password'.encode('utf-8'))
            else:
                client_socket.send('Wrong sign command'.encode('utf-8'))
        client_socket.close()
'''