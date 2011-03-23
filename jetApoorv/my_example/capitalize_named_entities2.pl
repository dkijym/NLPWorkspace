#!/usr/bin/perl -w

# these words are always lowercased
@lc_words = ('a','an','and', 'by','for','from','in','of','or','the','to','with');
for (@lc_words) {$is_lc_word{$_} = 1}

# arabic prefixes in hyphenated words get either left the same or lowercased
@arabic_prefixes = ('al', 'el', 'bin', 'abu');
for (@arabic_prefixes) {$is_arabic_prefix{$_} = 1}

# arabic prefixes in hyphenated words that get lowercased
@arabic_prefixes_tolower = ('al', 'el');
for (@arabic_prefixes_tolower) {$is_arabic_prefix_tolower{$_} = 1}

# these standalone arabic words get lowercased
@standalone_words_tolower = ('al', 'bin');
for (@standalone_words_tolower) {$is_standalone_word_tolower{$_} = 1}

# no need for these sice the default rule of uppercasing would apply
#@standalone_words_toraise = ('abu', 'el');
#for (@standalone_words_toraise) {$is_standalone_word_toraise{$_} = 1}

@capitalize_nothing = (0,1,2,10);
for (@capitalize_nothing) {$is_capitalize_nothing{$_} = 1}

@capitalize_first = (5,7,8,9,13,14);
for (@capitalize_first) {$is_capitalize_first{$_} = 1}

@capitalize_all = (3,4,6,11,12,15,16,17);
for (@capitalize_all) {$is_capitalize_all{$_} = 1}


sub capitalize_word
{
 my $str = $_[0];
 
 if($str =~ /-/)
 {
  @parts = split /-/, $str;
  
  my @first_word = ();
  
  if($is_arabic_prefix{lc $parts[0]})
  {
   my $prefix = shift @parts;
   $prefix = lc $prefix if $is_arabic_prefix_tolower{lc $prefix};
   push @first_word, $prefix;
  }
  
  @fixed_parts = map capitalize_word($_), @parts;
  return join '-', (@first_word, @fixed_parts);
 }
 
 
 elsif($is_lc_word{lc $str})
 {
  return lc $str;
 }
 elsif($is_standalone_word_tolower{lc $str})
 {  return lc $str;
 }
 else
 {
  return ucfirst(lc($str));
 }
 
}

sub capitalize($)
{
 my $str = $_[0];
 
 #print STDERR "Capitalizing: ", $str, "\n";

 $str =~ s/[-\S]+/capitalize_word($&)/ge;
 
 return $str;
}

sub capitalize_template_text($)
{
 @parts = split /(\[.*?\])/, $_[0]; 

 my $is_first_part = 1;

 my @result;

 for my $part (@parts)
 {
  if($part =~ /^\s*\[/)
  {
   push @result, $part;
  }
  else
  {
   $part = lc $part;
   $part =~ s/^\s*[a-z]/uc($&)/e if $is_first_part;
  }
  $is_first_part = 0;
 }

 return join '', @parts;
}

undef $/;
open F, $ARGV[0];
$_ = <F>;
close F;

/template-number=(['"])(\d+)\1/;
$template_number = $2;

s/(?<=<query-text>).*?(?=<\/query-text>)/ capitalize_template_text($&) /e;

if($is_capitalize_all{$template_number})
{
 print STDERR "Capitalizing all fields\n";
 s/\[(.+?)\]/'[' . capitalize($1). ']'/ge;
 s/(<arg-value[^>]*?>)(.+?)(<\/arg-value>)/ $1 . capitalize($2) . $3/ge;
}
elsif($is_capitalize_first{$template_number})
{
 print STDERR "Capitalizing only the first field\n";
 s/\[(.+?)\]/'[' . capitalize($1). ']'/e;
 s/(<arg-value[^>]*?>)(.+?)(<\/arg-value>)/ $1 . capitalize($2) . $3/e;
}
elsif($is_capitalize_nothing{$template_number})
{
 print STDERR "Not capitalizing any fields\n";
}
else
{
 print STDERR "Unknown tempate \"$template_number\": skipping capitalization\n";
}

print;
