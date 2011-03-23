averageSalary(range1)
{
	x=FindFindFirstNode <= range1
	statistics =  averageSalarylessThan(ROOT, x)
	return statistics.totalSalary / statistics.numberPeople
}

averageSalaryLessThan(Root, x)
{
	numberPeople = x.left.size + 1
	totalSalary = x.left.totalSalary = x.totalSalary
	y=x
	while y != Root
	{
		if( y == y.p.right)
		{
			numberPeople += x.left.size + 1
			totalSalary += x.left.totalSalary = x.totalSalary
		}
		y=y.p
	}
	return (numberPeople , totalSalary)
}
FindFirstNode<=(T, x)
{
	y= T
	if(y.key == x.key)
		return y
	if(x.key < y.key)
		return x
	return FindFirstNode<=	
}

averageSalary(range1,range2)
{
	x=FindFindFirstNode <= range2
	range2Statistics = averageSalaryLessThan(ROOT, x)
	y = FindFirstNode >= range1
	range1Statistics = averageSalaryLessThan(ROOT, y)
	return (range2Statistics.totalSalary - range1Statistics.totalSalary) / (range2statistics.numberPeople - range1statistics.numberPeople)
}