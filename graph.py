
file = open("filename.txt","r")

from matplotlib import pyplot as plt

x = []
y = []

for line in file:
    if not line:
        continue
    arr = line.strip().split(",")
    x.append(float(arr[0]))
    y.append(float(arr[1]))
plt.plot(x,y, label="average error")
plt.title("Probability and Average Error")
plt.ylabel("Average Error")
plt.xlabel("Probability")
plt.legend()
plt.show()