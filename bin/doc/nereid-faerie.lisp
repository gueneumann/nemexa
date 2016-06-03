(in-package :nereid)

#||
Implementation of faeri Algorithm by Li, Dong, Feng, SIGMOD, 2011

I will implement the single heap algorithm first

I consider a unit as a document for which the single-heap based method is applied.
I assume that a unit is a proper cognitive unit so that a NE does not crossover two units.
I assume that the ngram index is specialized for faeri using 
(make-faerie-gazetters-inverted-index) -> 

OBACHT: koennte gross/kleinschribung relevant sein ?

NOTE: 
I am using a hashtable for counting substrings instead of the 2-dim array.

||#

;;; ********************************************************************************
;;; basically should define this as a clos object

(defstruct (faerie-container)
  ngram-list ;; the ngram representation of the document
  ngram-list-length ;; number of ngrams
  inverted-lists ;; inverted list of each ngram
  position-list ;; sorted  position of each inverted list the top element of the heap is a member of
  min-heap ;; the min heap
  toplevel-element ;; the poped element of the heap
  count-array ;; the array that records an entity's occurence number for each possible substring of the document
  valid-substring-interval ;; store the lower/upper bounds for current entity
  lower-entity-length ;; T_l
  )

;;; function that initializes major data structures and sets variables along the line of SIGMOD 2011 paper
;;; also works for :token level, but need specific print functions (I guess so)

(defun init-nereid-faerie (&key (ngram 3) (ngram-fct :char))
  (init-nereid-adm)
  (setf (nereid-adm-parameters-ngram-size *nereid-parameters*) ngram) ;;
  (setf (global-params-start-end *global-params*) NIL) ;; no special characters for start/end are allowed
  
  (setf (nereid-adm-parameters-sim-threshold *nereid-parameters*) 2) ;; as in paper
  (setf (nereid-adm-parameters-sim-function *nereid-parameters*) :ed) ;; as in paper

  (setf (nereid-adm-parameters-ngram-fct *nereid-parameters*) ngram-fct)
  )

#|
This is to get the relevant entities from the database and to compute the position list 
Steps:
1. get the inverted lists for each ngram of the input document
2. for each top element of  a inverted list create a position list
   using counter pos; if position list is new, then also add top element to heap
   otherwise just adjust position list (avoid keeping duplicate elements in heap)

NOTE: weak point currently: copying of selected lists; should use pointer instead
-> checked it! does not improve much !
|#

;;; Initialization function for main function nereid-faerie() for each unit D
;;; Initialize inverted lists and min heap and position lists

(defun get-ngram-values-flat (ngram gaz-database)
  "This assumes that the value of an ngram is a list of indices if entries"
  (copy-list (gethash ngram (gaz-database-ngrams-index gaz-database)))
  )

;;; not useful so far, because fearie needs sorted values, which can be done offline when loading the MWL dictionary
;;; but not if MWL index would be structured as for cpmerge
;;; so using cpmerge index structure would require an additional online sorting which is nix gudd

(defun get-ngram-values-hash (ngram gaz-database)
  "This assumes that the value of an ngram is a hashtable keeping for each possible lenght the corresponding entry"
  (loop for value being the hash-values of  
       (gethash ngram (gaz-database-ngrams-index gaz-database))
     append 
       ;; no copy necessary because append does it
     value)
  )

(defun initialize-faerie-container (ngram-list gaz-database)
  (let ((faerie-container (make-faerie-container 
			   :ngram-list (first ngram-list)
			   :ngram-list-length (rest ngram-list)
			   :inverted-lists NIL
			   :position-list (make-hash-table :test #'eql :size 100)
			   :min-heap (make-instance 'cl-heap:fibonacci-heap :sort-fun #'<)
			   ;; GN: Define a hashtable for keeping track counts of 
			   ;; substring spans not 2-dim array
			   :count-array (make-hash-table :test #'equal :size 50)))
	;; NOTE: position actually starts from 1 as in paper, so have to initialize variable pos with 0
	(pos 0))
    (loop for ngram in (faerie-container-ngram-list faerie-container)
       do
	 (let* ((ngram-values-list (get-ngram-values-flat ngram gaz-database))
		;; retrieve inverted index for ngram -> I could use here also the inverted structure of NEMEX-A
		;; so to have a uniform index -> no, because so far sorting is necessary

		;; copy the inverted list 
		;; -> could avoid this if I would use pointers to the corresponding top element
		;; -> see nereid-faerie-pointer.lisp, but does not show imporovement
		;; Using first element in inverted list means that we assume that the index of entities
		;; are in increasing order
		(top-element (when ngram-values-list (first ngram-values-list))) ;; top elements are not poped
		)
	   ;; determine position of ngram in the tokenized document
	   ;; so now, pos starts from 1
	   (incf pos)
	   
	   ;; collect all positions of ngram in input document

	   ;; if top-element exists then
	   ;; extend its position list or 
	   ;; create position list and add element to heap
	   ;; initially, the order is in decrasing position
	   (when top-element
	     (multiple-value-bind (val found)
		 (gethash top-element (faerie-container-position-list faerie-container))
	       (cond (found 
		      (setf (gethash top-element (faerie-container-position-list faerie-container)) 
			    ;; make sure that position list is in correct increasing order
			    (append val (list pos))))
		     (t (setf (gethash top-element (faerie-container-position-list faerie-container))
			      (list pos))
			;; only if position list is new a heap element has to be created
			(cl-heap:add-to-heap (faerie-container-min-heap faerie-container) 
					     top-element))
		     )))
	   ;; save copied list for ngram found at ith position in ith position of 
	   ;; (faerie-container-inverted-lists faerie-container)
	   (push ngram-values-list (faerie-container-inverted-lists faerie-container))
	   )
	 )

    (setf (faerie-container-inverted-lists faerie-container)
	  ;; restore order of inverted list according to order in input
	  (nreverse (faerie-container-inverted-lists faerie-container)))
    ;; pop first element from heap
    (setf (faerie-container-toplevel-element faerie-container)
	  (cl-heap:pop-heap (faerie-container-min-heap faerie-container)))
    faerie-container
    )
  )

;;; ********************************************************************************
;;; the occurrence array 

;;; Li et al claim one could use vectors !!! -> OK, but I am using a hashtable, i.e., 
;;; a mapping of a span to counts

;;; init-count-array is a data structure to record occurrence counts for each e on basis
;;; of its lower and upper bound 
;;; It is similar to the item set in Early parsing

;;; let D[i,l] be a substring of unit D, starting at position i with length l
;;; Given an entity e, use V[i][l] to count e's occurence in D[i,l]'s inverted lists
;;; In order to use it here: D[1, l=ngram-list-of-entity]
;;; Range of l via compute-min-max-size-of-target-strings(e)
;;; -> see file nereid-identification.lisp
;;;
;;; NOTE: I AM USING A HASHTABLE TO STORED COUNTS TO SPANS; THE KEY IS A SPAN, AND THE VALUE THE COUNT

;;; both functions are changed to handle hashtable
(defun init-count-hash ()
  (make-hash-table :test #'equal :size 50))

(defun count-items (faerie-container)
  ;; all entries in hash table have been seen at least one time, so this
  ;; number of entries in hashtable are just the number of items
  (hash-table-count (faerie-container-count-array faerie-container))	
  )

#|
Core loop for single-based heap method: counting occurence of entities for all valid substrings

1. initialize V for top element e
2. for all positions of e in D do
3. determine its relevant entries in V and increase its counter
4. if value in V increases threshold T (min-overlap), then pair (D[i,l],e) is a candidate pair

5. adjust heap

|#

;;; 1. initialze V for entity boundaries for e (see footnote 4 in paper of Li et al. 2011)
;;; entity = (aref (gaz-database-entries *gaz-database*) entity-index)
;;; computation is based on similarity function and threshold
;;; ********************************************************************************
;;; NOTE: I AM USING A HASHTABLE INSETAD OF A VECTOR AS PROPOSED BY THE FAERIE AUTHORS

(defun initialize-entity-boundaries (entity-length faerie-container sim-threshold sim-function)
  (setf (faerie-container-valid-substring-interval faerie-container)
	;; compute lower and upper bounds for overlap function for given entity, i.e., TAU_e und T_e
	(compute-min-max-size-of-target-strings entity-length sim-threshold sim-function))
  (setf (faerie-container-count-array faerie-container) (init-count-hash))
  )

;;; compute T_L -> used in pruning strategies
(defun init-lowerbound-on-entity-length (faerie-container entity-length sim-threshold sim-fct)
  (setf (faerie-container-lower-entity-length faerie-container)
	(compute-lowerbound-on-entity-length entity-length sim-threshold sim-fct))
    )

;;; 2. get position list of the inverted lists for an entity

(defun get-entity-position-list (entity-index faerie-container)
  (gethash entity-index (faerie-container-position-list faerie-container))
  )

;;; 3. for i-th element in position list, for relevant substrings increase counter in count-array
;;; a substring is a string that ends at ith position, and starts from a left position which is computed
;;; using the length restrictions of entity e
#|
Only D[i-l+1,l], ..., D[i,l], with TAU_entity <= l <= T_entity can contain i-th inverted list;
Do iteration for l taking care of proper setting of i when computing (D-index starts at 1, V-index starts at 0)
V[i-l+1][l], ..., V[i][l]
also note: second dimension actually is an index for l, i.e., V[0][0] means: 1-st element with length min-val
THIS CHANGED:
I STORE [I,L] AS A SPAN-INDEX AND USE THIS AS KEY FOR COUNTING

Steps: 
0. ith-position is right boundary for substrings
1. get lower (min-val) and upper (max-val) bound for entity e
2. for all distances l:
3. compute start position start-from of substring according to i-l+1,l
(i-l+1 for same reason as for determining second dimension of count array)
(if start position is less then 0, set it to 0)
4. loop for start-from to ith-position, and increase counter in V[i,l]
NOTE: documents start with index 1, count array starts with index 0, hence
have to take care of proper alignemnt;
NOTE: second dimension of array corresponds to index of different distances, hence
have to compute by normalizing it with l with min-val
|#

;;; ********************************************************************************
;;; NOTE: I AM USING A HASHTABLE INSETAD OF A VECTOR AS PROPOSED BY THE FAERIE AUTHORS
;;; which is a mapping from spans to occurrence number of a span
;;; makes sense here, because usually 2-dim array is very sparse, so a lot of potential spans
;;; are empty, i.e., have 0 value

(defun count-occurrence-of-entity-at-ith-position (ith-position faerie-container)
  (let ((min-val (first (faerie-container-valid-substring-interval faerie-container)))
	(max-val (rest (faerie-container-valid-substring-interval faerie-container))))
    (loop for l from min-val to max-val do
       (let* ((start-index (+ (- (1- ith-position) l) 1)) 
	      ;; jump l steps back from i-position -> to take into account sim-threshold
	      ;; if start-index points outside dimension then set it to 0
	      (start-from (if (> start-index 0) start-index 0)))
	 (loop for i from start-from to (1- ith-position) do 
	      (when (> (+ i l) (faerie-container-ngram-list-length faerie-container))
		;; substring D[i,l] has less then l tokens and hence is not valid
		;; which happens if ith position is too close to the end of the document and l is too large
		;; so that right boundary of document would be broken
		(Return :less-than-l-tokens))
	      
	      (let ((span (cons i l)))
		(multiple-value-bind (val found)
		    (gethash span (faerie-container-count-array faerie-container))
		  (cond (found 
			 (setf (gethash span (faerie-container-count-array faerie-container)) (incf val)))
			(t (setf (gethash span (faerie-container-count-array faerie-container)) 1)))))
	      )))))

;;; for each e's ngram and position in the complete unit count occurrence of entity in all valid windows (or substrings) 
;;; constrained by the length restrictions

(defun enumerate-candidate-pairs (entity-position-list faerie-container)
  (loop for position in entity-position-list do
       (count-occurrence-of-entity-at-ith-position position faerie-container)
       )
  )

;;; Step 4.: read-off candidates for entity

#|
-> The pair entity e and substring D[i,l] is a candidate pair, if 
   occurence number V[i,l] >= T, where T is min-overlap(length(e), length(D[i,l]), sim-fct, sim-threshold)
-> how and where to call this function efficiently ?
-> I will do it after count-array has been created for entity, because otherwise everytime a counter
   is increased I would have to check it

THIS ALSO CHANGED TO MAKE USE OF HASHTABLE: BENEFIT NOW: ONLY SPANS WHICH ARE SEEN AT LEAST ONE TIME ARE ACTUALLY 
VISITED NOT THE COMPETE 2-DIM ARRAY AS BEFORE
|#

(defun get-candidate-pairs (entity-index entity-length faerie-container sim-threshold sim-fct)
  (let ((cands nil))
    (loop for key being the hash-key of (faerie-container-count-array faerie-container) 
       using (hash-value val) when 
       ;; counter should be no smaller than the min-overlap
       	 (>= val
	     ;; this computes threshold T
	     ;; rest key is just l
	     (min-overlap entity-length (rest key) sim-threshold sim-fct))
       do
       ;; if so, push candidate and entity
       	 (push (cons key entity-index) cands))
    cands)
  )

  
;;; ********************************************************************************
;;; Adjusting of relevant data structures, after an top element of the heap has been processed
#|
Adjust faerie container:

 - pop top element instances from inverted lists  
 - select top elements from these inverted lists
   insert them into position list using positions of old top element; 
   check if already exists, then update -> order not important here -> not sure: must be sorted !
 - insert them into the heap
- remove old top element from position list
 - pop top element from heap
   and assign it as new toplevel-element

|#

;;; NOTE:
;; now make sure position list is sorted -> sort it !
;; or add element via merging
;; 
;; I HATE IT, but it seemed to be necessary in order for the pruning stuff to work !
;; can i avoid it ???
;; -> at least delay it until the pruner is called who needs a sorted list
;; either do it here, and then using merge
;; (merge 'list (gethash top-element (faerie-container-position-list faerie-container)) (list pos) #'<)
;; or in pruning method using sort()

(defun adjust-faerie-container (entity-index entity-position-list faerie-container)
  (let ((top-elements NIL))
    (loop for pos in entity-position-list do
       ;; remove old top element from pos-th inverted list; (1- pos) because of function nth()
       ;; this determines the inverted list at position pos
	 (pop (nth (1- pos) (faerie-container-inverted-lists faerie-container)))
       ;; get the new top element
	 (let ((top-element 
		(first (nth (1- pos) (faerie-container-inverted-lists faerie-container))))
	       )
	   ;; and store its position pos in its position list
	   ;; if it already exists then extend it otherwise  create ot
	   (when top-element
	     (multiple-value-bind (val found)
		 (gethash top-element (faerie-container-position-list faerie-container))
	       (cond (found 
		      (setf (gethash top-element (faerie-container-position-list faerie-container)) (cons pos val))
		      ;;(setf (gethash top-element (faerie-container-position-list faerie-container)) (merge 'list val (list pos) #'<))
		      )
		     (t (setf (gethash top-element (faerie-container-position-list faerie-container)) 
			      (list pos))
			;; if top-element already has a position list, it has been seen already earlier and
			;; hence is already in the heap
			;; thus only new top elements will be inserted into the heap
			(cl-heap:add-to-heap  (faerie-container-min-heap faerie-container) top-element)
			)))
	     )
	   )
	 )

    ;; remove old top-element and its position list from the hash array
    ;; should be ok because top-element will never occur in rest of inverted lists because
    ;; inverted lists are assumed to be sorted in increasing order and are also visited via the min heap
    ;; in that order
    (remhash entity-index (faerie-container-position-list faerie-container))

    ;; pop first element from heap
    (setf (faerie-container-toplevel-element faerie-container)
	  (cl-heap:pop-heap (faerie-container-min-heap faerie-container)))
    )
  )

;;; ********************************************************************************
;;; Pruning

;;; ********************************************************************************
;;; 1. Lazy-count pruning: 
;;; if length of entity-position-list is < T_l, then prune entity
;;;    which mean: do not enumerate-candidate-pairs

;;; NOTE: enumeration of candidate pairs is applied on complete input string
;;;       where bucket and binary apply it only only designated substrings of the input

(defun lazy-count-pruning (entity-position-list faerie-container)
  (< (length entity-position-list)
     (faerie-container-lower-entity-length faerie-container)
     )
  )

;;; ********************************************************************************
;;; 2. Bucket-Count pruning
;;;
;;; a bucket: a subsequence of the position list, with "close-enough" elements
#|
Formally: given two neighbor elements from P_e, p_i and p_i+1, any substring containing
both elements, has at least (p_i+1 - p_i - 1) mismatched elements. -> These are just the elements
that are between p_i and p_i+1. Since p_i and p_i+1 belong to entity e, the elements between them
cannot belong to them. Thus if the the size of these other elements is too large, e can be ignored
for this substring.
|#

;;; TAU = ( T_e - T_l )

(defun unvalid-neighbor-distance (p1 p2 Tau)
  (> (- p2 p1 1) Tau))

;;; NOTE: I assume that the elements in the position list are ordered in increasing order
;;; thus the elements in a bucket are collected in decreasing order inside this method
;;; 
;;; make-bucket() starts with an initialized bucket using the first element of position list
;;; and checkes for the remaining elements whether they are to be added to the bucket. For the first
;;; element for which this does not hold, return the bucket and the remaining position list
;;; (make-bucket 2 '(2 3 4 9 14 19) '(1)) -> ((4 3 2 1) 9 14 19)

;;; the two functions here are tail-recursive so should be ok for the compiler
(defun make-bucket (approximate-entity-length entity-position-list-rest bucket)
  (if (null entity-position-list-rest)
      (cons bucket entity-position-list-rest)
      (if (unvalid-neighbor-distance (first bucket) ;; bucket is built in reserved order
				     (first entity-position-list-rest)
				     approximate-entity-length)
	  (cons bucket entity-position-list-rest)
	  (make-bucket approximate-entity-length 
			 (rest entity-position-list-rest)
			 (cons (first entity-position-list-rest) bucket)))
      ))

;;; Now, compute all buckets for complete position list
;;; for each determined bucket, count entity occurrency
;;; -> this is what I mean by incremental approach
;;; This is how I understand the text on page 534 (seond paragraph)
;;; I think this is correct, because a position list stores the ngram occurence of 
;;; an entity e in the complete document, and as just, all possible occurrencies of e in 
;;; the document. A bucket represent a single possible occurrency
;;;

(defun make-buckets (approximate-entity-length entity-position-list faerie-container)
  (when entity-position-list
    (let ((new-bucket (make-bucket approximate-entity-length 
				   (rest entity-position-list)
				   (list (first entity-position-list)))))
      (unless (lazy-count-pruning  (first new-bucket) faerie-container)
	;; only if lazy pruning for bucket is passed do counting
	;; this means only if current sublist of position list has enough sharing elements with e
	;; determine the candidate substrings in s and count its occurrence
	;; this also implies that fewer candidate pairs will be generated and hence the count hash will 
	;; have fewer elements or fewer counts
	(enumerate-candidate-pairs (first new-bucket) faerie-container))
      (make-buckets approximate-entity-length (rest new-bucket) faerie-container)
      )))

;;; main caller for bucket-count-pruning
;;; sort position list, compute threshold, compute buckets

(defun bucket-count-pruning (entity-position-list faerie-container)
  (let ((sorted-entity-position-list (sort (copy-list entity-position-list) #'<))
	;; sorting is necessary, because I do not reorder the stack of IL elements 
	;; when I insert new elements
	;; when adjusting a faerie-container !
	;; I have to test, what performs better: merging or sorting 
	(approximate-entity-length 
	 ;; compute ( T_e - T_l )
	 ;; according to paper, we could also define stronger bounds for the different similarity functions
	 ;; here for the value of approximate-entity-length
	 (- (rest (faerie-container-valid-substring-interval faerie-container))
	    (faerie-container-lower-entity-length faerie-container))))
    ;; actually compute the buckets
    (make-buckets approximate-entity-length sorted-entity-position-list faerie-container))
  )

;;; ********************************************************************************
;;; 3. Find candidate windows -> Algorithm 1 in paper Li et al. 2011
;;; binary pruning method:

;;; Three functions
;;; - find-candidate-window
;;; - binaryspan
;;; - binaryshift
;;;
;;; This version works, does not generate duplicates but different size of output
;;; compared to :no and the others -> check why

(defun binary-count-pruning (entity-position-list faerie-container)
  (let ((tau-e (rest (faerie-container-valid-substring-interval faerie-container)))
	;; tau-e = upper bound of token numbers
	(T-l (faerie-container-lower-entity-length faerie-container))
	;; T-l = pruning threshold for approximating |intersect(e,s)| -> approximate lower bound
	(sorted-entity-position-list (sort (copy-list entity-position-list) #'<)))
    (find-candidate-windows sorted-entity-position-list T-l tau-e faerie-container) 
    )
  )

;;; Find possible valid windows and from these find all candidate windows
;;; instead of creating and testing all possible valid windows, try to shift
;;; to candidate windows directly: 
#|
A window is a sublist of P_e
A valid window is a window with T-l <= P_e[i,...,j] <= tau_e
A candidate window is a valid window with t_e <= D[p_i,...,p_j]<= T_e
Number of all possible valid windows:
(lambda (sum 0) (loop for l from T-l to tau-e do  (setf (+ sum (1+ (- (length p_e) l))))))

Binary Span and Shift based method: The basic idea is as
follows. Given a valid window Pe[i · · · j], if pj −pi +1 > TAUe,
we will not shift to Pe[(i + 1) · · · (j + 1)]. Instead we want
to directly shift to the first possible candidate window after
i, denoted by Pe[mid · · · (mid + j − i)], where mid satisfies
pmid+j−i − pmid + 1 ≤ TAU-e and for any i ≤ mid' < mid,
pmid'+j−i − pmid' + 1 > TAU-e. Similarly, if pj − pi + 1 ≤ TAU-e
we will not iteratively span it to Pe[i · · · (j +1)], 
Pe[i · · · (j + 2)], . . . , Pe[i · · · x]. 
Instead, we want to directly span to the
last possible candidate window starting with i, denoted by
Pe[i · · · x], where x satisfies px − pi + 1 ≤ TAU-e and for any
x'> x, px' − pi + 1 > TAU-e
|#

(defun find-candidate-windows (p-e t-l tau-e faerie-container)
  (let ((i 1)
	(len-p-e (length p-e)))
    (loop while (<= i (1+ (- len-p-e t-l))) 
       ;; left paranthesis -> moves from left to right until plausible leftmost position is reach
       do
	 (let ((j (1- (+ i t-l)))) 
	   ;; right position of sublist. j depends on value of i.
	   ;;(format T "~&Position list: ~a, span: ~a, ~a, while: ~a " p-e i j (1+ (- len-p-e t-l)))
	   (if (>= j i) 
	       ;; p-e is a sorted list, so this should not be violated
	       ;; should it not be the case that (> j i) ? -> but see definition 3 of Faerie paper
	       (cond ((<= (1+ (- (nth (1- j) p-e) (nth (1- i) p-e))) tau-e)
		      ;; p_j - p_i +1
		      ;; make sure that |Pe[i · · · j]| ≤ TAU-e
		      ;; togetether this means we have a valid window
		      ;; with size
		      ;; t-l ≤ |Pe[i · · · j]| ≤ TAU-e
		      ;; see text page 534, under figure 6
		      ;; now find candidate window
		      ;; ⊥e ≤ |D[pi · · · pj ]| ≤ TAU-e
		      (binary-span i j p-e tau-e faerie-container)
		      (incf i)) ;; shift to the next window
		     (T (setf i (binary-shift i j p-e t-l tau-e))))
	       (return :bad-interval)
	       )
	   )
	 )
    )
  )

#|
The binary span operation can directly span to Pe[i · · · x]
and has two advantages. Firstly, in many applications, users
want to identify the best similar pairs (sharing common tokens
as many as possible), and the binary span can efficiently
find such substrings. Secondly, we do not need to
find candidates of e for Pe[i · · · (j + 1)], . . . , Pe[i · · · x] one
by one. Instead since there may be many candidates between
lo = pj −TAU-e + 1 and up = pi+x−j + TAU-e − 1, we find
them in a batch manner. We group the candidates based on
their token numbers. Entities in the same group have the
same number of tokens. Consider the group with g tokens,
suppose Tg is the threshold computed using |e| and g. If
|Pe[i · · · x]| < Tg, we prune all candidates in the group.
|#

(defun enumerate-candidate-window (candidate-window tau-e faerie-container)
  ;; entspricht dieser test dem lazy-count pruning ?
  (when (<= (+ 1 (- (first (reverse candidate-window)) 
		    (first  candidate-window)))
	    tau-e)
    (enumerate-candidate-pairs candidate-window faerie-container)))

;;; strange: when I change i and j, no difference ??
(defun binary-span (i j p-e tau-e faerie-container)
  (let ((lower j)
	(upper (1- (+ i tau-e)))
	(mid 0)
	(len-p-e (length p-e)))
    (loop while (<= lower upper)
       do
	 (setf mid (ceiling (/ (+ upper lower) 2)))
       ;; mid is right element in p-e so can maximally be (length p-e)
	 (if (and 
	      ;; GN: mid should be between 1 and (length p-e)
	      (>= mid 1) (<= mid len-p-e)
	      (> (1+ (- (nth (1- mid) p-e) (nth (1- i) p-e))) tau-e)
	      )
	     (setf upper (- mid 1))
	     (setf lower (+ mid 1))))
    ;; GN: added this test, otherwise length restriction of p-e is violated
    (setf mid (if (>= upper len-p-e) len-p-e upper))
    (enumerate-candidate-window (subseq p-e (1- i)  mid) 
				tau-e faerie-container)
    )
  )

;;; binary shift can directly shift unnecessary valid windows
;;; instead of step-by-step
;;; relevant value that binary shift returns is the starting point i
;;; core idea is to jump many steps instead of one  by one
;;; the new position is the position of the first possible valid window

(defun binary-shift (i j p-e t-l tau-e)
  (let ((lower i)
	(upper j)
	(len-p-e (length p-e)))
    (loop while (<= lower upper)
       do
	 (let ((mid (ceiling (/ (+ lower upper) 2))))
	   (if (> (1+ (- (+ (nth (1- j) p-e) (- mid i)) (nth (1- mid) p-e))) tau-e)
	       (setf lower (+ mid 1))
	       (setf upper (- mid 1)))))
    (setf i lower)
    (setf j (1- (+ i t-l)))
    (if (and 
	 ;; GN: j should be between i and (length p-e)
	 (>= j i) (<= j len-p-e)
	 (> (1+ (- (nth (1- j) p-e) (nth (1- i) p-e))) tau-e))
	(setf i (binary-shift i j p-e t-l tau-e))
	i)))

;;; ********************************************************************************

;;; main caller for single-heap method
;;;
;;; item-counter is used to count the number of non-zero elements in the count array, 
;;; which needs to be verified
;;; Still a bit unclear to me, what verification means;

(defun nereid-faerie (unit-string
		      &key
		      (sim-threshold (nereid-adm-parameters-sim-threshold *nereid-parameters*))
		      (sim-fct (nereid-adm-parameters-sim-function *nereid-parameters*))
		      (gaz-database *gaz-database*)
		      (print-method :no-details)
		      (count-items T)
		      (with-pruning :lazy)
		      )
  (let* ((ngram-list (compute-ngram-wrapper (nereid-adm-parameters-ngram-fct *nereid-parameters*)
					    unit-string))
	 (faerie-container (initialize-faerie-container ngram-list gaz-database))
	 (all-candidate-pairs nil)
	 (item-counter 0)
	 )

    (loop  while (not (cl-heap::is-empty-heap-p 
		       (faerie-container-min-heap faerie-container)))
       do
	 (let* ((entity-index (faerie-container-toplevel-element faerie-container))
		(entity (aref (gaz-database-entries gaz-database) entity-index))
		(entity-length (gaz-element-ngram-size entity)) ;; entity is only called here
		(entity-position-list  (get-entity-position-list entity-index faerie-container))
		(candidate-pairs NIL))

	   ;;(format T "~&Position list[~a, ~a]: ~a~%" entity-index entity (sort (copy-list entity-position-list) #'<))
	   
	   ;;; Basically it should also be possible to call here
	   ;;; (initialize-entity-boundaries entity-length faerie-container sim-threshold sim-fct)
	   ;;; And from this we can compute the lower-bound trivially
	   (init-lowerbound-on-entity-length faerie-container entity-length sim-threshold sim-fct)

	   (setf candidate-pairs
		 (case with-pruning
		   (:lazy 
		    (unless 
			(lazy-count-pruning entity-position-list faerie-container)
		      ;; also initializes count-array for entity
		      (initialize-entity-boundaries entity-length faerie-container sim-threshold sim-fct)
		      (enumerate-candidate-pairs entity-position-list faerie-container)
		      (get-candidate-pairs entity-index entity-length 
					   faerie-container sim-threshold sim-fct)
		      ))
		   (:bucket
		    (unless 
			(lazy-count-pruning entity-position-list faerie-container)
		      (initialize-entity-boundaries entity-length faerie-container sim-threshold sim-fct)
		      (bucket-count-pruning entity-position-list faerie-container)
		      (get-candidate-pairs entity-index entity-length 
					   faerie-container sim-threshold sim-fct)
		      ))
		   (:binary
		    (unless 
			(lazy-count-pruning entity-position-list faerie-container)
		      (initialize-entity-boundaries entity-length faerie-container sim-threshold sim-fct)
		      (binary-count-pruning entity-position-list faerie-container)
		      (get-candidate-pairs entity-index entity-length 
					   faerie-container sim-threshold sim-fct)
		      ))
		   (:no
		    (initialize-entity-boundaries entity-length faerie-container sim-threshold sim-fct)
		    (enumerate-candidate-pairs entity-position-list faerie-container)
		    (get-candidate-pairs entity-index entity-length 
					 faerie-container sim-threshold sim-fct)
		    )
		   (otherwise (error "Choose one of :lazy :bucket :binary :no ") :done)
		   )
		 )
	   
	   (when count-items
	     (setf item-counter (+ item-counter (count-items faerie-container)))
	     )

	   (setf all-candidate-pairs (append candidate-pairs all-candidate-pairs))
	   
	   (adjust-faerie-container entity-index entity-position-list faerie-container))
	 )
    
    (when count-items
      (format T "~&Items: ~d~%" item-counter))
    (get-nereid-faerie-result print-method all-candidate-pairs 
			      unit-string ngram-list gaz-database)
    )
  )

;;; ********************************************************************************
;;; Printing the candidate pairs

;;; a dummy function for returning the found candidate pairs, which is a list of pairs
;;; consisting of the span of the substring and the index of the identified entity

(defun get-nereid-faerie-result (print-method candidate-pairs unit-string ngram-list gaz-database)
  (case print-method
      (:details (get-span-candidates-and-entities candidate-pairs unit-string gaz-database))
      (:pairs (print-candidate-pairs candidate-pairs))
      (:no-details (length candidate-pairs))
      (:prune (prune-get-span-candidates-and-entities candidate-pairs unit-string gaz-database))
      )
  )

;;; this works only for ngram-fct :char
;;; for :token I actually the span corresponds to the position of the found entries in the
;;; list of tokens, so I need to compute the correct span or need to use the token-list form the faerie-container


(defun get-span-candidates-and-entities (candidate-pairs unit-string gaz-database)
  (loop for x in candidate-pairs collect
       ;; starting from 0 and ending at n-1 because of function subseq()
       (let ((left (caar x))
	     (right (+ (- (nereid-adm-parameters-ngram-size *nereid-parameters*)
			  1)
		       (caar x) (cdar x))))
	 (cons 
	  ;; the span of the substring in the input string adjusted for used ngram size
	  ;;(first x)
	  (cons left right)	       
	  (cons 
	   ;; the substring from the input
	   ;; NOTE: end position is computed by (min-val + l) (see get-candidate-pairs()
	   
	   (subseq unit-string left right)
	   ;; the entity
	   (first (aref (gaz-database-entries gaz-database) (rest x))))))
       )
  )

;;; idea: check and eventually prune a candidate pair
;;; currently: if it contains a space

(defun prune-get-span-candidates-and-entities (candidate-pairs unit-string gaz-database)
  (loop for x in candidate-pairs append
       ;; starting from 0 and ending at n-1 because of function subseq()
       (let* ((left (caar x))
	      (right (+ (- (nereid-adm-parameters-ngram-size *nereid-parameters*)
			   1)
			(caar x) (cdar x)))
	      (text-fragment (subseq unit-string left right)))
	 (unless
	     (contains-blank text-fragment)
	   (list
	    (cons 
	    ;; the span of the substring in the input string adjusted for used ngram size
	    ;;(first x)
	    (cons left right)	       
	    (cons 
	     ;; the substring from the input
	     ;; NOTE: end position is computed by (min-val + l) (see get-candidate-pairs()
	     
	     (subseq unit-string left right)
	     ;; the entity
	     (first (aref (gaz-database-entries gaz-database) (rest x))))))
	   )
	 )
       )
  )

(defun contains-blank (string)
  (loop for x across string 
     do
       (if (char= x #\ ) (return t))
       )
  )

(defun print-candidate-pairs (candidate-pairs)
  candidate-pairs
  )