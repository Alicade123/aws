#!/usr/bin/env python3

def is_prime(num):
    if num < 2:
        return False
    for i in range(2, int(num**0.5) + 1):
        if num % i == 0:
            return False
    return True

# Find all prime numbers between 1 and 250
prime_numbers = [num for num in range(1, 251) if is_prime(num)]

# Display them on the screen
print("Prime numbers between 1 and 250:")
for prime in prime_numbers:
    print(prime)

# Store the results in a results.txt file
with open("results.txt", "w") as f:
    for prime in prime_numbers:
        f.write(f"{prime}\n")

print("\nResults successfully saved to results.txt")